package it.unical.ea.Travel.Mappers.user;

import it.unical.ea.Travel.DTOs.user.UserDTO;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Entities.user.UserType;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        String fullName;
        if (user.getUserType() == UserType.SOCIETA) {
            fullName = user.getCompanyName();
        } else {
            fullName = (user.getFirstName() != null ? user.getFirstName() : "")
                     + " "
                     + (user.getLastName() != null ? user.getLastName() : "");
            fullName = fullName.trim();
        }
        
        return new UserDTO(
            user.getId(),
            user.getEmail(),
            user.getUserType(),
            user.getFirstName(),
            user.getLastName(),
            user.getCompanyName(),
            user.getVatNumber(),
            user.getDocumentPhotos(),
            user.getPhone(),
            fullName
        );
    }
}
