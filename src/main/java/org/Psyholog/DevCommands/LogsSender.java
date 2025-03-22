package org.Psyholog.DevCommands;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Ticket.DataStorage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class LogsSender extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        TextChannel logsTextChannel = guild.getTextChannelById(Dotenv.load().get("logsChannel"));
        TextChannel textChannelEvent = event.getChannel().asTextChannel();
        Role psyhologRole = guild.getRoleById(Dotenv.load().get("psyhologRole"));
        String targetTextChannelId = getKeyByValue(DataStorage.getInstance().getTicketChannelMap(), textChannelEvent.getId());
        if (targetTextChannelId != null) {
            if (event.getMember().getRoles().contains(psyhologRole)) {
                // Получаем текущую дату и время
                LocalDateTime now = LocalDateTime.now();

                // Форматируем дату в нормальный вид
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                String formattedDateTime = now.format(formatter);
                Member psyholog = event.getMember();
                String message = event.getMessage().getContentRaw().toString();
                String ticketDesc = DataStorage.getInstance().getTicketDes().get(targetTextChannelId);
                logsTextChannel.sendMessage(  "`Номер тикета: " + targetTextChannelId + "`" + "\n" + "`Описание тикета: " + ticketDesc + "`" +
                        "\n\n" + "`" + formattedDateTime + " " + psyholog.getEffectiveName() + " (" + psyholog.getId() + "):` " + message
                        + "\n`----------------------------------------------------------------`").queue();
            }
        } else {
            return;
        }
    }

    public static String getKeyByValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
