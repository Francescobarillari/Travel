package it.unical.ea.Travel.DTOs.location;

import java.math.BigDecimal;

public record LocationRequestDTO(
        String name,
        String address,
        String city,
        String country,
        BigDecimal latitude,
        BigDecimal longitude) {
}
