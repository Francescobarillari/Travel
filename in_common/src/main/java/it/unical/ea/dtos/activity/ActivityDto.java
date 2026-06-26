package it.unical.ea.dtos.activity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ActivityDto {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
}