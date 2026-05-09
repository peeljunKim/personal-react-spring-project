package org.personal.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.personal.project.dto.payment.PaymentCompleteRequest;
import org.personal.project.dto.payment.PaymentPrepareResponse;
import org.personal.project.dto.payment.PaymentSyncResponse;
import org.personal.project.service.payment.PaymentPrepareService;
import org.personal.project.service.payment.PaymentSynchronizer;
import org.personal.project.service.payment.PortOneWebhookService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * 결제 API
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentPrepareService paymentPrepareService;
    private final PaymentSynchronizer paymentSynchronizer;
    private final PortOneWebhookService portOneWebhookService;

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @PostMapping("/prepare")
    public PaymentPrepareResponse prepare(Principal principal) {
        return paymentPrepareService.prepare(principal.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @PostMapping("/complete")
    public PaymentSyncResponse complete(@Valid @RequestBody PaymentCompleteRequest request) {
        return paymentSynchronizer.synchronize(request.getPaymentId());
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> webhook(@RequestBody String rawBody, @RequestHeader HttpHeaders headers) {
        portOneWebhookService.handle(rawBody, headers);
        return ResponseEntity.ok().build();
    }
}
