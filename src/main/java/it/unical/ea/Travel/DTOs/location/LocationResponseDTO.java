package it.unical.ea.Travel.DTOs.location;

import java.math.BigDecimal;
import java.util.UUID;

public record LocationResponseDTO(
        UUID id,
        String name,
        String address,
        String city,
        String country,
        BigDecimal latitude,
        BigDecimal longitude) {
}
