package it.unical.ea.Travel.DTOs.location;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocationRequestDTO(
        @NotBlank(message = "{name.NotBlank.location}")
        @Size(max = 255, message = "{name.Size.location}")
        String name,
        @Size(max = 255, message = "{address.Size.location}")
        String address,
        @NotBlank(message = "{city.NotBlank.location}")
        @Size(max = 100, message = "{city.Size.location}")
        String city,
        @NotBlank(message = "{country.NotBlank.location}")
        @Size(max = 100, message = "{country.Size.location}")
        String country,
        @DecimalMin(value = "-90.0", message = "{latitude.DecimalMin.location}")
        @DecimalMax(value = "90.0", message = "{latitude.DecimalMax.location}")
        @Digits(integer = 3, fraction = 7, message = "{latitude.Digits.location}")
        BigDecimal latitude,
        @DecimalMin(value = "-180.0", message = "{longitude.DecimalMin.location}")
        @DecimalMax(value = "180.0", message = "{longitude.DecimalMax.location}")
        @Digits(integer = 3, fraction = 7, message = "{longitude.Digits.location}")
        BigDecimal longitude) {
}
