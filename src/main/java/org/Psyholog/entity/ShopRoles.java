package org.Psyholog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "shop_roles")
public class ShopRoles {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "cost", nullable = false)
    private int cost;

    public int getCost() {return cost;}
    public void setCost(int cost) {this.cost = cost;}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
}
