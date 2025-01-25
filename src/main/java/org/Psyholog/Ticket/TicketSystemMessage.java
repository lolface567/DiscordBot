package org.Psyholog.Ticket;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TicketSystemMessage extends ListenerAdapter {
    private static Message currentMessage; // The message that we will update
    private static Timer timer; // Timer for scheduling updates

    public static void execute(SlashCommandInteractionEvent event) {
        try {
            Guild guild = event.getGuild();
            TextChannel channel = guild.getTextChannelById(Dotenv.load().get("embedMessage"));

            if (channel != null) {
                System.out.println("Отправка сообщения в канал: " + channel.getName());
                sendOrUpdateMessage(channel, guild, event);
            } else {
                System.err.println("Не удалось найти канал ");
                event.reply("Не удалось найти канал").setEphemeral(true).queue();
            }
        } catch (Exception e) {
            System.err.println("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }
        event.reply("Сообщение отправлено").setEphemeral(true).queue();
    }

    private static void sendOrUpdateMessage(TextChannel channel, Guild guild, SlashCommandInteractionEvent event) {
        EmbedBuilder embedBuilder = createEmbedBuilder(guild);

        if (currentMessage == null) {
            // Send new message
            channel.sendMessageEmbeds(embedBuilder.build())
                    .setComponents(ActionRow.of(Button.success("ticket", "\uD83D\uDCDD Связаться")))
                    .queue(success -> {
                        currentMessage = success;
                        System.out.println("Сообщение отправлено успешно: " + success.getId());
                        startUpdateTask(channel, guild); // Start updating task
                    }, error -> {
                        System.err.println("Ошибка при отправке сообщения: " + error.getMessage());
                        error.printStackTrace();
                        if (event != null) {
                            event.reply("Ошибка при отправке сообщения: " + error.getMessage()).setEphemeral(true).queue();
                        }
                    });
        } else {
            // Update existing message
            currentMessage.editMessageEmbeds(embedBuilder.build())
                    .setComponents(ActionRow.of(Button.success("ticket", "\uD83D\uDCDD Связаться")))
                    .queue(success -> System.out.println("Сообщение обновлено успешно: " + success.getId()),
                            error -> {
                                System.err.println("Ошибка при обновлении сообщения: " + error.getMessage());
                                error.printStackTrace();
                                if (event != null) {
                                    event.reply("Ошибка при обновлении сообщения: " + error.getMessage()).setEphemeral(true).queue();
                                }
                            });
        }
    }

    private static EmbedBuilder createEmbedBuilder(Guild guild) {
        Role psychologistRole = guild.getRoleById(Dotenv.load().get("psyhologRole"));
        if (psychologistRole == null) {
            System.err.println("Роль психолога не найдена!");
            return new EmbedBuilder().setTitle("Ошибка").setDescription("Роль психолога не найдена").setColor(Color.RED);
        }

        List<Member> psychologists = guild.getMembersWithRoles(psychologistRole);
        int onlinePsychologists = (int) psychologists.stream()
                .filter(member -> member.getOnlineStatus().equals(OnlineStatus.ONLINE))
                .count();

        return new EmbedBuilder()
                .setTitle("Связаться с психологом")
                .setColor(Color.DARK_GRAY)
                .setDescription("**Нажав на кнопку** \uD83D\uDCDD **__Связаться__**, Вы создадите отдельное анонимное обращение и Вам ответит первый освободившийся психолог.\n" +
                        "\n" +
                        "\uD83D\uDCE2 **ВНИМАНИЕ:** Если Вы не предупредили психолога о своем отсутствии в обращении, Ваш диалог может быть удален через 30 минут.\n" +
                        "\n" +
                        "\uD83D\uDEAB **ПРЕДУПРЕЖДЕНИЕ:** За создание обращений, содержащих троллинг, Вы гарантированно получите роль @\uD83D\uDEAB, с которой Вы навсегда потеряете доступ к созданию обращений.\n" +
                        "\n" +
                        "─ \uD83D\uDCC3 **Обращений открыто:** " + "**" + DataStorage.getInstance().getUserActiveTickets().size() + "**" +
                        "\n" +
                        "\n" +
                        "─ \uD83D\uDCC4 **Обратилось за все время:** " + "**" + DataStorage.getInstance().getTicketCounter() + "**" +
                        "\n" +
                        "\n" +
                        "─ \uD83D\uDE4D\u200D♂\uFE0F **Психологов онлайн:** " + "**" + onlinePsychologists + "**" +
                        "\n" +
                        "\n" +
                        "Часы активной работы психологов: 9:00-22:00 по МСК.")
                .setFooter("Created by NONAME")
                .setTimestamp(java.time.Instant.now());
    }

    private static void startUpdateTask(TextChannel channel, Guild guild) {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendOrUpdateMessage(channel, guild, null);
            }
        }, 60000, 60000); // Schedule to run every minute
    }
}
