package com.recoverai.service;

import com.recoverai.domain.payment.Payment;
import com.recoverai.domain.recovery.CaseState;
import com.recoverai.domain.recovery.RecoveryCase;
import com.recoverai.dto.RazorpayWebhookPayload;
import com.recoverai.repository.PaymentRepository;
import com.recoverai.repository.RecoveryCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final PaymentRepository paymentRepository;
    private final RecoveryCaseRepository recoveryCaseRepository;
    private final AuditService auditService;

    @Transactional
    public void processWebhook(RazorpayWebhookPayload payload) {
        if (!"payment.failed".equals(payload.getEvent())) {
            return;
        }

        RazorpayWebhookPayload.Entity entity = payload.getPayload().getPayment().getEntity();

        // 1. Persist the Payment (include order_id for later payment.captured correlation)
        Payment payment = Payment.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .method(entity.getMethod())
                .customerId(entity.getCustomerId())
                .contact(entity.getContact())
                .email(entity.getEmail())
                .errorCode(entity.getErrorCode())
                .errorDescription(entity.getErrorDescription())
                .orderId(entity.getOrderId())
                .createdAt(OffsetDateTime.now())
                .build();
        paymentRepository.save(payment);

        // 2. Create RecoveryCase (RECEIVED -> ELIGIBLE)
        UUID caseId = UUID.randomUUID();
        RecoveryCase recoveryCase = RecoveryCase.builder()
                .id(caseId)
                .paymentId(payment.getId())
                .status(CaseState.ELIGIBLE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        recoveryCaseRepository.save(recoveryCase);

        // ===== STAGE 4: CASE INSERTED =====
        log.info("[WEBHOOK-STAGE-4] RecoveryCase created: id={} status={} paymentId={}",
                caseId, recoveryCase.getStatus(), payment.getId());

        // 3. Write Audit Events
        auditService.logEvent(caseId, "WEBHOOK_VERIFIED", Map.of(
            "source", "razorpay",
            "event_id", payload.getId() != null ? payload.getId() : "evt_unknown",
            "signature_verified", true,
            "event", payload.getEvent() != null ? payload.getEvent() : "payment.failed",
            "payment_id", payment.getId() != null ? payment.getId() : "",
            "amount", payment.getAmount() != null ? payment.getAmount() : 0,
            "error_code", payment.getErrorCode() != null ? payment.getErrorCode() : "UNKNOWN"
        ));

        log.info("[WEBHOOK-STAGE-4] WEBHOOK_VERIFIED audit written for caseId={} eventId={}", caseId, payload.getId());
        
    }

    @Transactional
    public void processPaymentLinkPaid(RazorpayWebhookPayload payload) {
        if (!"payment_link.paid".equals(payload.getEvent())) {
            return;
        }

        RazorpayWebhookPayload.Entity entity = payload.getPayload().getPaymentLink().getEntity();
        if (entity.getNotes() == null || !entity.getNotes().containsKey("case_id")) {
            log.warn("[WEBHOOK-PAYMENT-LINK] No case_id found in notes for payment_link.paid event {}", payload.getId());
            return;
        }

        UUID caseId;
        try {
            caseId = UUID.fromString(entity.getNotes().get("case_id"));
        } catch (IllegalArgumentException e) {
            log.warn("[WEBHOOK-PAYMENT-LINK] Invalid case_id in notes for event {}", payload.getId());
            return;
        }

        RecoveryCase rc = recoveryCaseRepository.findById(caseId).orElse(null);
        if (rc == null) {
            log.warn("[WEBHOOK-PAYMENT-LINK] RecoveryCase not found for id {}", caseId);
            return;
        }

        rc.setStatus(CaseState.RECOVERED);
        rc.setActualRecoveredAmount(entity.getAmount());
        rc.setUpdatedAt(OffsetDateTime.now());
        recoveryCaseRepository.save(rc);

        String outcomeMsg = String.format("Customer completed payment via recovery link. ₹%s recovered, confirmed via payment_link.paid webhook at %s.",
                entity.getAmount(), OffsetDateTime.now());

        auditService.logEvent(caseId, "OUTCOME", Map.of(
                "status", "RECOVERED",
                "reason", outcomeMsg,
                "amount", entity.getAmount()
        ));

        log.info("[WEBHOOK-PAYMENT-LINK] Case id={} marked RECOVERED. amount={}", caseId, entity.getAmount());
    }

    /**
     * Handles payment.captured webhook — the success signal for SEND_EMAIL recovery cases.
     * When a customer retries the original failed order and succeeds, Razorpay fires payment.captured
     * with the same order_id as the original failed payment. We look up the original payment by order_id,
     * find the associated recovery case in WAITING/ELIGIBLE/RETRY, and mark it RECOVERED.
     */
    @Transactional
    public void processPaymentCaptured(RazorpayWebhookPayload payload) {
        if (!"payment.captured".equals(payload.getEvent())) {
            return;
        }

        RazorpayWebhookPayload.Entity entity = payload.getPayload().getPayment().getEntity();
        String orderId = entity.getOrderId();

        if (orderId == null || orderId.isBlank()) {
            log.info("[WEBHOOK-CAPTURED] No order_id in payment.captured entity id={} — cannot correlate to recovery case", entity.getId());
            return;
        }

        // Find the original failed payment by order_id
        Payment originalPayment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (originalPayment == null) {
            log.info("[WEBHOOK-CAPTURED] No payment found with order_id={} — not a RecoverAI-tracked case", orderId);
            return;
        }

        // Find the recovery case in a recoverable state
        java.util.List<com.recoverai.domain.recovery.CaseState> recoverableStates = java.util.List.of(
                com.recoverai.domain.recovery.CaseState.WAITING,
                com.recoverai.domain.recovery.CaseState.ELIGIBLE,
                com.recoverai.domain.recovery.CaseState.RETRY
        );
        com.recoverai.domain.recovery.RecoveryCase rc =
                recoveryCaseRepository.findFirstByPaymentIdAndStatusIn(originalPayment.getId(), recoverableStates).orElse(null);

        if (rc == null) {
            log.info("[WEBHOOK-CAPTURED] payment.captured for order_id={} paymentId={} but no active recovery case found (already closed or not tracked)",
                    orderId, originalPayment.getId());
            return;
        }

        java.math.BigDecimal capturedAmount = entity.getAmount(); // already converted from paise in WebhookController
        rc.setStatus(com.recoverai.domain.recovery.CaseState.RECOVERED);
        rc.setActualRecoveredAmount(capturedAmount);
        rc.setUpdatedAt(OffsetDateTime.now());
        recoveryCaseRepository.save(rc);

        String outcomeMsg = String.format(
                "Customer retried original order and payment was captured. ₹%s recovered, confirmed via payment.captured webhook at %s.",
                capturedAmount, OffsetDateTime.now());

        auditService.logEvent(rc.getId(), "OUTCOME", Map.of(
                "status", "RECOVERED",
                "reason", outcomeMsg,
                "amount", capturedAmount,
                "trigger", "payment.captured",
                "order_id", orderId,
                "new_payment_id", entity.getId()
        ));

        log.info("[WEBHOOK-CAPTURED] Case id={} marked RECOVERED via payment.captured. orderId={} amount={}",
                rc.getId(), orderId, capturedAmount);
    }
}
