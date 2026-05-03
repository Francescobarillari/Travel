package it.unical.ea.Travel.Mappers.user;

import it.unical.ea.Travel.DTOs.user.UserDTO;
import it.unical.ea.Travel.Entities.user.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        String fullName = user.getFirstName() + " " + user.getLastName();
        return new UserDTO(user.getId(), fullName);
    }
}
