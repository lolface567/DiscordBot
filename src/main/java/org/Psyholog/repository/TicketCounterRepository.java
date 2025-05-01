package org.Psyholog.repository;

import org.Psyholog.entity.TicketCounter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCounterRepository extends JpaRepository<TicketCounter, Integer> {
}
