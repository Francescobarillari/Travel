package it.unical.ea.dtos.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCaptureRequestDto {
    private String orderId;
    private String bookingId;
    private String bookingType; // "ITINERARY" or "ACTIVITY"
}
