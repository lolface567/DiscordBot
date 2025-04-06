package org.Psyholog.DevCommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Ticket.DataStorage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TicketLogs extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        if (event.getChannel() instanceof TextChannel) { // Проверяем, является ли канал текстовым
            TextChannel textChannelEvent = event.getChannel().asTextChannel();
            if (DataStorage.getInstance().isTextChannelExists(textChannelEvent.getId())) {
                if (!event.getMember().getUser().isBot()) {
                    if (textChannelEvent != null) {
                        // Получаем текущую дату и время
                        LocalDateTime now = LocalDateTime.now();
                        String IdName = DataStorage.getInstance().getTicketIdName(textChannelEvent.getId());

                        // Форматируем дату в нормальный вид
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                        String formattedDateTime = now.format(formatter);

                        Member member = event.getMember();

                        String message = event.getMessage().getContentRaw().toString();
                        String ticketDesc = DataStorage.getInstance().getTicketDescription(Integer.parseInt(IdName));

                        File logDir = new File("logs");
                        if (!logDir.exists()) {
                            logDir.mkdirs(); // Создает папку, если её нет
                        }
                        File logs = new File(logDir, IdName + ".txt");
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
                                Member psyholog = guild.getMemberById(DataStorage.getInstance().getPsychologist(Integer.parseInt(IdName)));
                                writer.write("==============================================================" +
                                        "\nНомер тикета: " + IdName +
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
