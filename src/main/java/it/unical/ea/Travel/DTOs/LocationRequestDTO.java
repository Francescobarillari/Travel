package it.unical.ea.Travel.DTOs;

import java.math.BigDecimal;

public record LocationRequestDTO(
        String name,
        String address,
        String city,
        String country,
        BigDecimal latitude,
        BigDecimal longitude) {
}
