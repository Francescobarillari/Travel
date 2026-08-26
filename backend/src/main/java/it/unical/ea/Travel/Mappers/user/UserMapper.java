package it.unical.ea.Travel.Mappers.user;

import it.unical.ea.dtos.user.UserDTO;
import it.unical.ea.dtos.user.UserPrivateDTO;
import it.unical.ea.dtos.user.UserPublicDTO;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.enums.UserType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Usiamo source = "." per passare l'intero oggetto User al metodo custom
    @Mapping(target = "fullName", source = ".", qualifiedByName = "mapFullName")
    @Mapping(target = "password", ignore = true)
    UserDTO toDTO(User user);

    @Mapping(target = "fullName", source = ".", qualifiedByName = "mapFullName")
    UserPublicDTO toPublicDTO(User user);

    @Mapping(target = "fullName", source = ".", qualifiedByName = "mapFullName")
    @Mapping(target = "password", ignore = true)
    UserPrivateDTO toPrivateDTO(User user);

    @Named("mapFullName")
    default String mapFullName(User user) {
        if (user == null) {
            return null;
        }

        // Recuperiamo la logica corretta dal vecchio mapper manuale
        if (user.getUserType() == UserType.SOCIETA) {
            return user.getCompanyName();
        } else {
            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            String lastName = user.getLastName() != null ? user.getLastName() : "";
            return (firstName + " " + lastName).trim();
        }
    }

    @org.mapstruct.AfterMapping
    default void mapAvatarUrl(User user, @org.mapstruct.MappingTarget UserDTO dto) {
        dto.setAvatarUrl(buildAvatarUrl(user));
    }

    @org.mapstruct.AfterMapping
    default void mapAvatarUrlPublic(User user, @org.mapstruct.MappingTarget UserPublicDTO dto) {
        dto.setAvatarUrl(buildAvatarUrl(user));
    }

    @org.mapstruct.AfterMapping
    default void mapAvatarUrlPrivate(User user, @org.mapstruct.MappingTarget UserPrivateDTO dto) {
        dto.setAvatarUrl(buildAvatarUrl(user));
    }

    default String buildAvatarUrl(User user) {
        if (user == null) {
            return null;
        }
        if (user.getAvatarUrl() != null) {
            if (user.getAvatarUrl().startsWith("http://") || user.getAvatarUrl().startsWith("https://")) {
                return user.getAvatarUrl();
            } else {
                try {
                    if (org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() != null) {
                        return org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/user/")
                                .path(user.getId().toString())
                                .path("/avatar")
                                .toUriString();
                    } else {
                        return "/api/user/" + user.getId() + "/avatar";
                    }
                } catch (Exception e) {
                    return "/api/user/" + user.getId() + "/avatar";
                }
            }
        } else {
            String seed;
            String style;
            if (user.getUserType() == UserType.SOCIETA) {
                seed = user.getCompanyName() != null ? user.getCompanyName() : (user.getId() != null ? user.getId().toString() : "default");
                style = "shape-grid";
            } else {
                seed = mapFullName(user);
                if (seed == null || seed.trim().isEmpty()) {
                    seed = user.getId() != null ? user.getId().toString() : "default";
                }
                style = "glyphs";
            }
            String encodedSeed = java.net.URLEncoder.encode(seed, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
            return "https://api.dicebear.com/10.x/" + style + "/png?seed=" + encodedSeed;
        }
    }
}