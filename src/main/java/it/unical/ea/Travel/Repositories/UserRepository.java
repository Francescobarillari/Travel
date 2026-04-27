package it.unical.ea.Travel.Repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.unical.ea.Travel.Entities.User;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    public Optional<User> getUserByEmail(String email);

    // bisogna cambiare questa query in sql -> JPA Specifications
    @Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
    Optional<User> findDeletedUserByEmail(String email);
}
