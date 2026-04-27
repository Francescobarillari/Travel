package it.unical.ea.Travel.DTOs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteListItemDTO {

    private UUID id;
    private LocalDateTime addedAt;
    private String notes;
    private UUID favoriteListId;
    private UUID experienceId;
}
