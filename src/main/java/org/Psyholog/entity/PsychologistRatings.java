package org.Psyholog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "psychologist_ratings")
public class PsychologistRatings {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Getter
    @Setter
    @Column(name = "psychologist_id", nullable = false)
    private String psychologist;

    @Getter
    @Setter
    @Column(name = "customer", nullable = false)
    private String customer;

    @Getter
    @Setter
    @Column(name = "ratings", nullable = false)
    private Double rating;
}
