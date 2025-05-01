package org.Psyholog.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
public class TicketCounter {

    @Id
    private int id = 1; // всегда 1

    @Getter
    @Setter
    private int counter;
}
