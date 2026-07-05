package it.unical.ea.Travel.Controllers.localita;

import it.unical.ea.Travel.Services.localita.LocalitaService;
import it.unical.ea.dtos.localita.LocalitaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/localita")
@RequiredArgsConstructor
public class LocalitaController {

    private final LocalitaService localitaService;

    @GetMapping("/search")
    public ResponseEntity<Page<LocalitaDto>> searchLocalita(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<LocalitaDto> results = localitaService.searchLocalita(query, page, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocalitaDto> getLocalitaById(@org.springframework.web.bind.annotation.PathVariable java.util.UUID id) {
        return localitaService.getLocalitaById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
