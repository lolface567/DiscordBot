package org.Psyholog.repository;

import jakarta.transaction.Transactional;
import org.Psyholog.entity.TicketsEntity;
import org.Psyholog.enumes.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketsRepository extends JpaRepository<TicketsEntity, Integer> {

    // 🔄 Назначить психолога
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE TicketsEntity t SET t.psychologistId = :psychologistId WHERE t.id = :ticketId")
    void assignPsychologist(@Param("ticketId") long ticketId, @Param("psychologistId") String psychologistId);

    // 🔄 Обновить статус
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE TicketsEntity t SET t.status = :status WHERE t.id = :ticketId")
    void updateStatus(@Param("ticketId") long ticketId, @Param("status") TicketStatus status);

    // 🔍 Получить text_channel_id по ID тикета
    @Query("SELECT t.textChannelId FROM TicketsEntity t WHERE t.id = :ticketId")
    String findTextChannelIdByTicketId(@Param("ticketId") long ticketId);

    @Query("SELECT COUNT(t) FROM TicketsEntity t WHERE t.status = 'open'")
    long countOpenTickets();

    @Query("SELECT COUNT(t) FROM TicketsEntity t WHERE t.psychologistId = :psychologistId AND t.status = 'closed'")
    long countClosedTicketsByPsychologist(@Param("psychologistId") String psychologistId);

    // 🔍 Проверка существования тикета по text_channel_id
    boolean existsByTextChannelId(String textChannelId);

    @Query("SELECT t.status FROM TicketsEntity t WHERE t.textChannelId = :ticketId")
    TicketStatus findStatusByTicketId(@Param("ticketId") long ticketId);

    // 🔍 Найти описание по text_channel_id
    @Query("SELECT t.description FROM TicketsEntity t WHERE t.textChannelId = :channelId")
    String findDescriptionByTextChannelId(@Param("channelId") String channelId);

    // 🔍 Найти тикет по text_channel_id
    Optional<TicketsEntity> findByTextChannelId(String text_channel_id);

    @Query("SELECT t.textChannelId FROM TicketsEntity t WHERE t.status = 'closed'")
    List<String> findAllClosedTextChannelIds();

    // 🔍 Найти тикет по user_id
    Optional<TicketsEntity> findByUserId(String user_id);

    // 🔍 Найти тикет по psychologist_id
    Optional<TicketsEntity> findByPsychologistId(String psychologist_id);

    // 🔍 Найти user_id по text_channel_id
    @Query("SELECT t.userId FROM TicketsEntity t WHERE t.textChannelId = :channelId")
    String findUserIdByTextChannelId(@Param("channelId") String channelId);

    // 🔍 Найти psychologist_id по text_channel_id
    @Query("SELECT t.psychologistId FROM TicketsEntity t WHERE t.textChannelId = :channelId")
    String findPsychologistIdByTextChannelId(@Param("channelId") String channelId);
}
