package com.recoverai.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.recoverai.repository.RecoveryCaseRepository;
import com.recoverai.repository.PaymentRepository;
import com.recoverai.repository.AuditEventRepository;
import com.recoverai.evaluation.DatasetGenerator;

@ExtendWith(MockitoExtension.class)
public class DemoDataInjectorMissingResourceTest {

    @Mock private RecoveryCaseRepository recoveryCaseRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private AuditEventRepository auditEventRepository;
    @Mock private DatasetGenerator datasetGenerator;
    @Mock private AuditService auditService;

    @Test
    void applicationContextLoadsWithoutCrashingWhenResourceIsMissing() {
        DemoDataInjector injector = new DemoDataInjector(
                recoveryCaseRepository, paymentRepository, auditEventRepository, datasetGenerator, auditService);

        try (MockedConstruction<ClassPathResource> mocked = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.exists()).thenReturn(false);
                })) {
            
            assertDoesNotThrow(() -> {
                injector.run();
            }, "Runner should load without crashing even if seed data is missing.");
        }
    }
}
