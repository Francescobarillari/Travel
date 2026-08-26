package it.unical.ea.Travel.Services.storage;

import it.unical.ea.Travel.Exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempUploadDir;

    private FileStorageService fileStorageService;

    // Byte signature per PNG minimo valido
    private static final byte[] VALID_PNG_BYTES = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89,
            0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54,
            0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05,
            0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4,
            0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
            (byte) 0xAE, 0x42, 0x60, (byte) 0x82
    };

    // Byte signature per JPEG minimo valido
    private static final byte[] VALID_JPEG_BYTES = new byte[]{
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,
            0x01, 0x01, 0x00, 0x60, 0x00, 0x60, 0x00, 0x00,
            (byte) 0xFF, (byte) 0xDB, 0x00, 0x43, 0x00,
            (byte) 0xFF, (byte) 0xD9
    };

    // Byte signature per WEBP minimo valido
    private static final byte[] VALID_WEBP_BYTES = new byte[]{
            'R', 'I', 'F', 'F', 0x12, 0x00, 0x00, 0x00,
            'W', 'E', 'B', 'P', 'V', 'P', '8', ' ',
            0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00
    };

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempUploadDir.toString());
        fileStorageService.init();
    }

    @Test
    @DisplayName("Salvataggio PNG con nome file privo di estensione salva correttamente come .png")
    void store_pngFileWithoutExtension_savesWithPngExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "custom_image",
                "application/octet-stream",
                VALID_PNG_BYTES
        );

        String storedPath = fileStorageService.store(file, "avatars");

        assertNotNull(storedPath);
        assertTrue(storedPath.startsWith("avatars/"));
        assertTrue(storedPath.endsWith(".png"), "Il file deve avere estensione .png derivata dal MIME type reale");

        Path savedFile = tempUploadDir.resolve(storedPath);
        assertTrue(Files.exists(savedFile), "Il file deve esistere sul filesystem");
    }

    @Test
    @DisplayName("Salvataggio JPEG con estensione dichiarata fittizia .png viene comunque salvato come .jpg")
    void store_jpegWithSpoofedPngExtension_savesWithJpgExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malicious_spoof.png",
                "image/png",
                VALID_JPEG_BYTES
        );

        String storedPath = fileStorageService.store(file, "activities");

        assertNotNull(storedPath);
        assertTrue(storedPath.startsWith("activities/"));
        assertTrue(storedPath.endsWith(".jpg"), "Il file deve essere salvato come .jpg in base al MIME reale rilevato");

        Path savedFile = tempUploadDir.resolve(storedPath);
        assertTrue(Files.exists(savedFile));
    }

    @Test
    @DisplayName("Salvataggio WEBP salva con estensione .webp")
    void store_webpFile_savesWithWebpExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.data",
                "application/octet-stream",
                VALID_WEBP_BYTES
        );

        String storedPath = fileStorageService.store(file, "itineraries");

        assertNotNull(storedPath);
        assertTrue(storedPath.startsWith("itineraries/"));
        assertTrue(storedPath.endsWith(".webp"));

        Path savedFile = tempUploadDir.resolve(storedPath);
        assertTrue(Files.exists(savedFile));
    }

    @Test
    @DisplayName("Rifiuto di file con MIME type non consentito (testo/script)")
    void store_invalidMimeType_throwsBadRequest() {
        byte[] textContent = "echo 'not an image'".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.jpg",
                "image/jpeg",
                textContent
        );

        ApiException ex = assertThrows(ApiException.class, () ->
                fileStorageService.store(file, "avatars")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("file.invalidType", ex.getMessage());
    }

    @Test
    @DisplayName("Rifiuto di file vuoto")
    void store_emptyFile_throwsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        ApiException ex = assertThrows(ApiException.class, () ->
                fileStorageService.store(file, "avatars")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("file.empty", ex.getMessage());
    }

    @Test
    @DisplayName("Rifiuto di file null")
    void store_nullFile_throwsBadRequest() {
        ApiException ex = assertThrows(ApiException.class, () ->
                fileStorageService.store(null, "avatars")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("file.empty", ex.getMessage());
    }

    @Test
    @DisplayName("Caricamento risorsa esistente")
    void load_existingFile_returnsResource() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "valid.png",
                "image/png",
                VALID_PNG_BYTES
        );

        String storedPath = fileStorageService.store(file, "avatars");
        Resource resource = fileStorageService.load(storedPath);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    @DisplayName("Caricamento risorsa inesistente lancia NotFound")
    void load_nonExistingFile_throwsNotFound() {
        ApiException ex = assertThrows(ApiException.class, () ->
                fileStorageService.load("avatars/non_existing_file.jpg")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("file.notFound", ex.getMessage());
    }

    @Test
    @DisplayName("Tentativo di path traversal in load lancia BadRequest")
    void load_pathTraversal_throwsBadRequest() {
        ApiException ex = assertThrows(ApiException.class, () ->
                fileStorageService.load("../../etc/passwd")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("file.invalidPath", ex.getMessage());
    }

    @Test
    @DisplayName("Eliminazione file esistente")
    void delete_existingFile_removesFromDisk() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                VALID_PNG_BYTES
        );

        String storedPath = fileStorageService.store(file, "avatars");
        Path filePath = tempUploadDir.resolve(storedPath);
        assertTrue(Files.exists(filePath));

        fileStorageService.delete(storedPath);
        assertFalse(Files.exists(filePath), "Il file deve essere stato eliminato");
    }

    @Test
    @DisplayName("Eliminazione con path nullo o vuoto non lancia eccezioni")
    void delete_nullOrBlankPath_doesNothing() {
        assertDoesNotThrow(() -> fileStorageService.delete(null));
        assertDoesNotThrow(() -> fileStorageService.delete("   "));
    }
}
