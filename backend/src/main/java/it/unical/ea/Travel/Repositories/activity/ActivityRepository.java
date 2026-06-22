package it.unical.ea.Travel.Repositories.activity;

import it.unical.ea.Travel.Entities.activity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    // I metodi standard (findAll, save, ecc.) gestiranno automaticamente 
    // il soft delete grazie alle annotazioni messe nell'Entity!
}