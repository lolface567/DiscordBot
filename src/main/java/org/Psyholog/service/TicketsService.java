package org.Psyholog.service;

import jakarta.transaction.Transactional;
import org.Psyholog.entity.TicketsEntity;
import org.Psyholog.enumes.TicketStatus;
import org.Psyholog.repository.TicketsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketsService {
    private final TicketsRepository ticketsRepository;

    @Autowired
    public TicketsService(TicketsRepository ticketsRepository) {
        this.ticketsRepository = ticketsRepository;
    }

    // 🆕 Создание тикета
    @Transactional
    public TicketsEntity createTicket(int id, String userId, String textChannelId, String description) {
        TicketsEntity ticket = new TicketsEntity();
        ticket.setId(id);
        ticket.setUserId(userId);
        ticket.setTextChannelId(textChannelId);
        ticket.setDescription(description);
        ticket.setStatus(TicketStatus.open);
        return ticketsRepository.save(ticket);
    }

    public List<String> getClosedTicketsList() {
        return ticketsRepository.findAllClosedTextChannelIds();
    }

    public TicketStatus getTicketStatus(long ticketId) {
        return ticketsRepository.findStatusByTicketId(ticketId);
    }

    // 🧠 Назначить психолога в тикет
    @Transactional
    public void assignPsychologist(int ticketId, String psychologistId) {
        ticketsRepository.assignPsychologist(ticketId, psychologistId);
    }

    // 🔒 Закрыть тикет
    @Transactional
    public void closeTicket(int ticketId) {
        ticketsRepository.updateStatus(ticketId, TicketStatus.closed);
    }

    public long getOpenTicketsCount() {
        return ticketsRepository.countOpenTickets();
    }

    public long getClosedTicketsCountByPsychologist(String psychologistId) {
        return ticketsRepository.countClosedTicketsByPsychologist(psychologistId);
    }

    // ✅ Проверка существования тикета по ID канала
    public boolean channelExists(String channelId) {
        return ticketsRepository.existsByTextChannelId(channelId);
    }

    // 🔍 Получить описание по ID канала
    public String getDescriptionByChannelId(String channelId) {
        return ticketsRepository.findDescriptionByTextChannelId(channelId);
    }

    // 🔍 Получить ID текстового канала по ID тикета
    public String getTextChannelIdByTicketId(int ticketId) {
        return ticketsRepository.findTextChannelIdByTicketId(ticketId);
    }

    // 🔍 Получить тикет по ID канала
    public Optional<TicketsEntity> getTicketByChannelId(String channelId) {
        return ticketsRepository.findByTextChannelId(channelId);
    }

    // 🔍 Получить тикет по ID пользователя
    public Optional<TicketsEntity> getTicketByUserId(String userId) {
        return ticketsRepository.findByUserId(userId);
    }

    // 🔍 Получить тикет по ID психолога
    public Optional<TicketsEntity> getTicketByPsychologistId(String psychologistId) {
        return ticketsRepository.findByPsychologistId(psychologistId);
    }

    // 🔍 Получить user_id по ID канала
    public String getUserIdByChannelId(String channelId) {
        return ticketsRepository.findUserIdByTextChannelId(channelId);
    }

    // 🔍 Получить psychologist_id по ID канала
    public String getPsychologistIdByChannelId(String channelId) {
        return ticketsRepository.findPsychologistIdByTextChannelId(channelId);
    }
}
