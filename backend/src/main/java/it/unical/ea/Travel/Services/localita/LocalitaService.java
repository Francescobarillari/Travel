package it.unical.ea.Travel.Services.localita;

import it.unical.ea.Travel.Entities.localita.Localita;
import it.unical.ea.Travel.Mappers.localita.LocalitaMapper;
import it.unical.ea.Travel.Repositories.localita.LocalitaRepository;
import it.unical.ea.dtos.localita.LocalitaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalitaService {

    private final LocalitaRepository localitaRepository;
    private final LocalitaMapper localitaMapper;

    @Transactional(readOnly = true)
    public Page<LocalitaDto> searchLocalita(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String safeKeyword = (keyword == null) ? "" : keyword.trim();
        Page<Localita> localitaPage = localitaRepository.searchByKeyword(safeKeyword, pageable);
        return localitaPage.map(localitaMapper::toDto);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<LocalitaDto> getLocalitaById(java.util.UUID id) {
        return localitaRepository.findById(id).map(localitaMapper::toDto);
    }
}
