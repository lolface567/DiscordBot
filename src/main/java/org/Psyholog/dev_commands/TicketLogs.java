package org.Psyholog.dev_commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.service.TicketCounterService;
import org.Psyholog.service.TicketsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TicketLogs extends ListenerAdapter {
    private static TicketsService ticketsService = null;

    @Autowired
    public TicketLogs(TicketsService ticketsService) {
        this.ticketsService = ticketsService;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        if (event.getChannel() instanceof TextChannel) { // Проверяем, является ли канал текстовым
            TextChannel textChannelEvent = event.getChannel().asTextChannel();
            if (ticketsService.channelExists(textChannelEvent.getId())) {
                if (!event.getMember().getUser().isBot()) {
                    if (textChannelEvent != null) {
                        // Получаем текущую дату и время
                        LocalDateTime now = LocalDateTime.now();
                        String idName = String.valueOf(
                                ticketsService.getTicketByChannelId(textChannelEvent.getId()).get().getId()
                        );

                        // Форматируем дату в нормальный вид
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                        String formattedDateTime = now.format(formatter);

                        Member member = event.getMember();

                        String message = event.getMessage().getContentRaw().toString();
                        String ticketDesc = ticketsService.getDescriptionByChannelId(textChannelEvent.getId());

                        File logDir = new File("logs");
                        if (!logDir.exists()) {
                            logDir.mkdirs(); // Создает папку, если её нет
                        }
                        File logs = new File(logDir, idName + ".txt");
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
                                Member psyholog = guild.getMemberById(ticketsService.getPsychologistIdByChannelId(textChannelEvent.getId()));
                                writer.write("==============================================================" +
                                        "\nНомер тикета: " + idName +
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
        else return;
    }
}
