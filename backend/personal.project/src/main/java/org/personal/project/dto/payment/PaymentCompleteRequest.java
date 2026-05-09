package org.personal.project.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentCompleteRequest {

    @NotBlank
    private String paymentId;
}
