package org.Psyholog.DevCommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Ticket.DataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ClearOpenCommand extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClearOpenCommand.class);
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if ("clear-open-tickets".equals(event.getName())) {
            // Создаем лист для хранения значений из ticketChannelMap
            List<String> ticketValues = new ArrayList<>(DataStorage.getInstance().getTicketChannelMap().values());

            // Добавляем все значения из ticketChannelMap в closedTickets
            DataStorage.getInstance().getClosedTickets().addAll(ticketValues);

            // Очищаем активные тикеты
            DataStorage.getInstance().getTicketChannelMap().clear();
            DataStorage.getInstance().getUserActiveTickets().clear();
            DataStorage.getInstance().saveData();

            // Опционально: предоставляем обратную связь пользователю
            event.reply("Все активные тикеты (" + ticketValues.size() + ") были перемещены в закрытые тикеты.").setEphemeral(true).queue();
            logger.info("Все активные тикеты (" + ticketValues.size() + ") были перемещены в закрытые тикеты.");
        }
    }
}