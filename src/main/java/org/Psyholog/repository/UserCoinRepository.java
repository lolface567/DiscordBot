package org.Psyholog.repository;

import jakarta.transaction.Transactional;
import org.Psyholog.entity.UserCoins;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCoinRepository extends JpaRepository<UserCoins, Long> {
    Optional<UserCoins> findById(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserCoins u SET u.coins = u.coins + :amount WHERE u.userId = :userId")
    void addCoinsByUserId(@Param("userId") Long userId, @Param("amount") int amount);

    @Modifying
    @Transactional
    @Query("UPDATE UserCoins u SET u.coins = u.coins - :amount WHERE u.userId = :userId")
    void removeCoinsByUserId(@Param("userId") Long userId, @Param("amount") int amount);
}