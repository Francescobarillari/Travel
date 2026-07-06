package it.unical.ea.dtos.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentResponseDto {
    private String clientSecret;
    private String bookingId;
}
