package it.unical.ea.Travel.Repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.unical.ea.Travel.Entities.User;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
}
