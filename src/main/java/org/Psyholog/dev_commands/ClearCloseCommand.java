package org.Psyholog.dev_commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.service.TicketCounterService;
import org.Psyholog.service.TicketsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClearCloseCommand extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClearCloseCommand.class);
    private static TicketsService ticketsService = null;

    @Autowired
    public ClearCloseCommand(TicketsService ticketsService, TicketCounterService ticketCounterService) {
        this.ticketsService = ticketsService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if ("clear-closed-tickets".equals(event.getName())) {
            List<String> ticketList = new ArrayList<>(ticketsService.getClosedTicketsList());
            Guild guild = event.getGuild();

            if (guild == null) {
                event.reply("Ошибка: гильдия не найдена.").setEphemeral(true).queue();
                return;
            }

            if (ticketList.isEmpty()) {
                event.reply("Нет закрытых тикетов для удаления.").setEphemeral(true).queue();
                return;
            }

            for (String ticketId : ticketList) {
                TextChannel textChannel = guild.getTextChannelById(ticketId);
                if (textChannel != null) {
                    textChannel.delete().queue(
                            success -> {
                                logger.info("Канал " + ticketId + " успешно удален.");
                            },
                            error -> {
                                logger.error("Ошибка при удалении канала " + ticketId + ": " + error.getMessage());
                            }
                    );
                } else {
                    logger.error("Канал с ID " + ticketId + " не найден.");
                }
            }
            event.reply("Все закрытые тикеты были успешно удалены.").setEphemeral(true).queue();
        }
    }
}
