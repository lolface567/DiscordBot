package org.Psyholog.repository;

import org.Psyholog.entity.ShopRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRolesRepository extends JpaRepository<ShopRoles, Long> {
    Optional<ShopRoles> findById(Long id);

    void deleteById(Long id);
}
