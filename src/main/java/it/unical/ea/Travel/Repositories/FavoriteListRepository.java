package it.unical.ea.Travel.Repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.unical.ea.Travel.Entities.FavoriteList;

@Repository
public interface FavoriteListRepository extends JpaRepository<FavoriteList, UUID> {

    List<FavoriteList> findByDeletedAtIsNull();

    List<FavoriteList> findByOwnerIdAndDeletedAtIsNull(UUID ownerId);
}
