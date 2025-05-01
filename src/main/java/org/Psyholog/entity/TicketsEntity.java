package org.Psyholog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.Psyholog.enumes.TicketStatus;

@Entity
@Table(name = "tickets")
public class TicketsEntity {
    @Getter
    @Setter
    @Id
    @Column(name = "id", nullable = false)
    private long id;

    @Getter
    @Setter
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Getter
    @Setter
    @Column(name = "psychologist_id")
    private String psychologistId;

    @Getter
    @Setter
    @Column(name = "text_channel_id", nullable = false)
    private String textChannelId;

    @Getter
    @Setter
    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;
}
