package it.unical.ea.Travel.Services.storage;

import it.unical.ea.Travel.Exception.ApiException;
import jakarta.annotation.PostConstruct;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Service
public class FileStorageService {

    private static final Map<String, String> MIME_TO_EXTENSION = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "application/pdf", ".pdf"
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = MIME_TO_EXTENSION.keySet();

    private final Tika tika = new Tika();

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "file.storageError");
        }
    }


    public String store(MultipartFile file, String subDir) {
        String detectedMimeType = detectAndValidateMimeType(file);
        String extension = getExtensionFromMimeType(detectedMimeType);
        String newFilename = UUID.randomUUID() + extension;

        Path targetDir = rootLocation.resolve(subDir).normalize();
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "file.storageError");
        }

        Path targetPath = targetDir.resolve(newFilename).normalize();


        if (!targetPath.startsWith(rootLocation)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "file.invalidPath");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "file.storageError");
        }

        return subDir + "/" + newFilename;
    }


    public Resource load(String filePath) {
        try {
            Path file = rootLocation.resolve(filePath).normalize();


            if (!file.startsWith(rootLocation)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "file.invalidPath");
            }

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ApiException(HttpStatus.NOT_FOUND, "file.notFound");
            }
        } catch (MalformedURLException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "file.notFound");
        }
    }


    public void delete(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        Path file = rootLocation.resolve(filePath).normalize();


        if (!file.startsWith(rootLocation)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "file.invalidPath");
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "file.storageError");
        }
    }

    private String detectAndValidateMimeType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "file.empty");
        }

        try {
            String detectedType = tika.detect(file.getInputStream());
            if (detectedType == null || !ALLOWED_CONTENT_TYPES.contains(detectedType)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "file.invalidType");
            }
            return detectedType;
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "file.readError");
        }
    }

    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return "";
        }
        return MIME_TO_EXTENSION.getOrDefault(mimeType, "");
    }
}
