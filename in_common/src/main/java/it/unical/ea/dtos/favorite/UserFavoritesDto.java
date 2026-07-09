package it.unical.ea.dtos.favorite;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserFavoritesDto {
    private Set<UUID> activityIds = new HashSet<>();
    private Set<UUID> itineraryIds = new HashSet<>();
}
