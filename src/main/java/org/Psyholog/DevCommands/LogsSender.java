package org.Psyholog.DevCommands;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Ticket.DataStorage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class LogsSender extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        TextChannel textChannelEvent = event.getChannel().asTextChannel();
        String targetTextChannelId = getKeyByValue(DataStorage.getInstance().getTicketChannelMap(), textChannelEvent.getId());
        if (!DataStorage.getInstance().getClosedTickets().contains(textChannelEvent.getId())) {
            if (!event.getMember().getUser().isBot()) {
                if (targetTextChannelId != null) {
                    // Получаем текущую дату и время
                    LocalDateTime now = LocalDateTime.now();

                    // Форматируем дату в нормальный вид
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                    String formattedDateTime = now.format(formatter);

                    Member member = event.getMember();

                    String message = event.getMessage().getContentRaw().toString();
                    String ticketDesc = DataStorage.getInstance().getTicketDes().get(targetTextChannelId);

                    File logDir = new File("logs");
                    if (!logDir.exists()) {
                        logDir.mkdirs(); // Создает папку, если её нет
                    }
                    File logs = new File(logDir, targetTextChannelId + ".txt");
                    boolean isNewFile = false;

                    // Проверяем, есть ли уже файл
                    if (!logs.exists()) {
                        try {
                            isNewFile = logs.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                    }

                    try (FileWriter writer = new FileWriter(logs, true)) {
                        if (isNewFile) {
                            Member psyholog = guild.getMemberById(DataStorage.getInstance().getTicketPsychologists().get(targetTextChannelId));
                            writer.write("==============================================================" +
                                    "\nНомер тикета: " + targetTextChannelId +
                                    "\nПсихолог тикета: " + psyholog.getEffectiveName() + " (" + psyholog.getId() + ") " +
                                    "\nОписание тикета: " + ticketDesc +
                                    "\n==============================================================");
                        }
                        writer.write("\n" + formattedDateTime + " - " + member.getEffectiveName() + " (" + member.getId() + ") - " + message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                return;
            }
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
