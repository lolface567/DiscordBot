package org.Psyholog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_coins")
public class UserCoins {
    @Id
    @Column(name = "id", nullable = false)
    private long userId;

    @Column(name = "coins", nullable = false)
    private int coins;

    // Геттеры и сеттеры
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }
}
