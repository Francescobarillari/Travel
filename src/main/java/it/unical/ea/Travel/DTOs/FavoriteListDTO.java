package it.unical.ea.Travel.DTOs;

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
public class FavoriteListDTO {

    private UUID id;
    private String name;
    private String description;
    private String visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID ownerId;
}
