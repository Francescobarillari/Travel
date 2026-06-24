package it.unical.ea.Travel.Mappers.user;

import it.unical.ea.Travel.DTOs.user.UserDTO;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Entities.user.UserType; // Aggiunto import
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Usiamo source = "." per passare l'intero oggetto User al metodo custom
    @Mapping(target = "fullName", source = ".", qualifiedByName = "mapFullName")
    @Mapping(target = "password", ignore = true)
    UserDTO toDTO(User user);

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
}