package it.unical.ea.Travel.DTOs.activity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ActivityDto {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}