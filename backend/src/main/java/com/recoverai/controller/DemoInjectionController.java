package com.recoverai.controller;

import com.recoverai.dto.RazorpayWebhookPayload;
import com.recoverai.evaluation.DatasetGenerator;
import com.recoverai.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Profile("!test")
public class DemoInjectionController {

    private final DatasetGenerator datasetGenerator;
    private final IngestionService ingestionService;
    private long lastInjectionTime = 0;

    @PostMapping("/inject")
    public ResponseEntity<?> injectSyntheticCases(@RequestParam(defaultValue = "50") int count) {
        if (System.currentTimeMillis() - lastInjectionTime < 10000) {
            return ResponseEntity.status(429).body(Map.of("error", "Too Many Requests. Please wait before injecting again."));
        }
        lastInjectionTime = System.currentTimeMillis();
        
        log.info("Injecting {} synthetic cases into the real pipeline...", count);

        // Generate synthetic cases
        List<DatasetGenerator.SyntheticCase> syntheticCases = datasetGenerator.generate(count, System.currentTimeMillis());

        int processed = 0;
        for (DatasetGenerator.SyntheticCase sc : syntheticCases) {
            try {
                // Construct a mock webhook payload from the synthetic payment
                RazorpayWebhookPayload payload = new RazorpayWebhookPayload();
                payload.setEvent("payment.failed");

                RazorpayWebhookPayload.Payload innerPayload = new RazorpayWebhookPayload.Payload();
                RazorpayWebhookPayload.PaymentEntity paymentEntity = new RazorpayWebhookPayload.PaymentEntity();
                RazorpayWebhookPayload.Entity entity = new RazorpayWebhookPayload.Entity();

                entity.setId(sc.getPayment().getId());
                entity.setAmount(sc.getPayment().getAmount());
                entity.setCurrency(sc.getPayment().getCurrency());
                entity.setStatus(sc.getPayment().getStatus());
                entity.setMethod(sc.getPayment().getMethod());
                entity.setCustomerId(sc.getPayment().getCustomerId());
                entity.setContact(sc.getPayment().getContact());
                entity.setErrorCode(sc.getPayment().getErrorCode());
                entity.setErrorDescription(sc.getPayment().getErrorDescription());

                paymentEntity.setEntity(entity);
                innerPayload.setPayment(paymentEntity);
                payload.setPayload(innerPayload);

                // Push through real ingestion pipeline
                ingestionService.processWebhook(payload);
                processed++;
            } catch (Exception e) {
                log.error("Failed to inject synthetic case for payment {}", sc.getPayment().getId(), e);
            }
        }

        log.info("Successfully injected {}/{} synthetic cases.", processed, count);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "requested", count,
                "injected", processed
        ));
    }
}
