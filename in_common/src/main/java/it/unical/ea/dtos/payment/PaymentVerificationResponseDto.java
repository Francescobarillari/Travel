package it.unical.ea.dtos.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationResponseDto {
    private boolean success;
    private String orderId;
    private String bookingId;
    private String bookingStatus;
    private String message;
}
