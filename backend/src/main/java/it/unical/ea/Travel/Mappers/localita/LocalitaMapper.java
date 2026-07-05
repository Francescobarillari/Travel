package it.unical.ea.Travel.Mappers.localita;

import it.unical.ea.Travel.Entities.localita.Localita;
import it.unical.ea.dtos.localita.LocalitaDto;
import org.springframework.stereotype.Component;

@Component
public class LocalitaMapper {

    public LocalitaDto toDto(Localita localita) {
        if (localita == null) {
            return null;
        }

        LocalitaDto dto = new LocalitaDto();
        dto.setId(localita.getId());
        dto.setName(localita.getName());
        dto.setDescription(localita.getDescription());

        return dto;
    }

    public Localita toEntity(LocalitaDto dto) {
        if (dto == null) {
            return null;
        }

        Localita localita = new Localita();
        localita.setId(dto.getId());
        localita.setName(dto.getName());
        localita.setDescription(dto.getDescription());

        return localita;
    }
}
