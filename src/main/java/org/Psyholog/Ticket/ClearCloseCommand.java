package org.Psyholog.Ticket;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ClearCloseCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if ("clear-closed-tickets".equals(event.getName())) {
            List<String> ticketList = new ArrayList<>(DataStorage.getInstance().getClosedTickets());
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
                                System.out.println("Канал " + ticketId + " успешно удален.");
                            },
                            error -> {
                                System.err.println("Ошибка при удалении канала " + ticketId + ": " + error.getMessage());
                            }
                    );
                } else {
                    System.err.println("Канал с ID " + ticketId + " не найден.");
                }
            }
            event.reply("Все закрытые тикеты были успешно удалены.").setEphemeral(true).queue();
        }
    }
}
