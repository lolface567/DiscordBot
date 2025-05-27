package org.Psyholog.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PsychologistProfile {
    @Id
    private String userId;

    private String username;

    public PsychologistProfile() {}

    public PsychologistProfile(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }
}
