package org.Psyholog.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.Psyholog.entity.TicketCounter;
import org.Psyholog.repository.TicketCounterRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketCounterService {

    private final TicketCounterRepository repository;

    private static final int SINGLE_COUNTER_ID = 1;

    @PostConstruct
    public void init() {
        if (!repository.existsById(SINGLE_COUNTER_ID)) {
            TicketCounter counter = new TicketCounter();
            counter.setCounter(0);
            repository.save(counter);
        }
    }

    public int getCurrentCount() {
        return repository.findById(SINGLE_COUNTER_ID)
                .orElseThrow(() -> new IllegalStateException("TicketCounter not initialized"))
                .getCounter();
    }

    @Transactional
    public int incrementAndGet() {
        TicketCounter counter = repository.findById(SINGLE_COUNTER_ID)
                .orElseThrow(() -> new IllegalStateException("TicketCounter not initialized"));

        int updated = counter.getCounter() + 1;
        counter.setCounter(updated);

        return updated;
    }
}
