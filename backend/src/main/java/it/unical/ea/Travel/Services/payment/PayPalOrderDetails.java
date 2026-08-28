package it.unical.ea.Travel.Services.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayPalOrderDetails {
    private String orderId;
    private String status;
    private BigDecimal amount;
    private String currency;
}
