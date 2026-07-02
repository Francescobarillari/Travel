package it.unical.ea.Travel.Repositories.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import it.unical.ea.enums.UserType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.unical.ea.Travel.Entities.user.User;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    public Optional<User> getUserByEmail(String email);
    public List<User> findByUserTypeAndApproved(UserType userType, Boolean approved);

    // bisogna cambiare questa query in sql -> JPA Specifications
    @Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
    Optional<User> findDeletedUserByEmail(String email);
}
