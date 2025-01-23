package org.Psyholog.Ticket;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CreateTicket extends ListenerAdapter {

    public static Map<String, String> userActiveTicketsMemory = new HashMap<>();
    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static final String feedbackChannel = Dotenv.load().get("feedbackChannel");
    public static final String adminChannel = Dotenv.load().get("adminChannel");
    public static final String ticketCategory = Dotenv.load().get("ticketCategory");
    public static final String psyhologRole = Dotenv.load().get("psyhologRole");
    public static final String closeTicketCategory = Dotenv.load().get("closeTicketCategory");
    public static final String voiceCategory = Dotenv.load().get("voiceCategory");


    public static void execute(ModalInteractionEvent event, String type, String age, String descriptionInput, String timeZone) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Ошибка: гильдия не найдена.").setEphemeral(true).queue();
            return;
        }

        Category category = guild.getCategoryById(ticketCategory);
        Member member = event.getMember();

        if (member == null) {
            event.reply("Ошибка: участник не найден.").setEphemeral(true).queue();
            return;
        }

        if (userActiveTicketsMemory.containsKey(member.getId())) {
            event.reply("Вы уже имеете активный тикет. Пожалуйста, завершите его, прежде чем создавать новый.").setEphemeral(true).queue();
            return;
        }

        if (category == null) {
            event.reply("Ошибка: категория не найдена.").setEphemeral(true).queue();
            return;
        }

        DataStorage.getInstance().incrementTicketCounter();
        String ticketNumber = String.valueOf(DataStorage.getInstance().getTicketCounter());
        String ticketName = "ticket-" + ticketNumber;
        DataStorage.getInstance().saveData();

        guild.createTextChannel(ticketName, category)
                .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .queue(textChannel -> {
                    DataStorage.getInstance().getTicketChannelMap().put(ticketNumber, textChannel.getId());
                    DataStorage.getInstance().getUserActiveTickets().put(textChannel.getId(), member.getId()); // Mark this ticket as active for the user
                    userActiveTicketsMemory.put(member.getId(), textChannel.getId()); // добавляет юзера в бан лист
                    DataStorage.getInstance().getTicketDes().put(ticketNumber, descriptionInput);
                    DataStorage.getInstance().saveData(); // Save data to file

                    TextChannel textChannelAdmin = guild.getTextChannelById(adminChannel);

                    Role role = guild.getRoleById(psyhologRole);

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder
                            .setTitle("🆕 Новое обращение")
                            .setColor(Color.DARK_GRAY)
                            .setDescription("Поступило новое обращение. Подробности ниже:")
                            .addField("📂 Тип:", type, false)
                            .addField("🎂 Возраст:", age, false)
                            .addField("📝 Описание проблемы:", DataStorage.getInstance().getTicketDes().get(ticketNumber), false)
                            .addField("\uD83D\uDD5D Часовой пояс:", timeZone, false)
                            .addField("📄 Ticket ID", ticketName, false)
                            .setFooter("Сообщение от " + member.getEffectiveName(), member.getUser().getAvatarUrl())
                            .setTimestamp(Instant.now());

                    EmbedBuilder embedBuilder1 = new EmbedBuilder()
                            .setColor(Color.DARK_GRAY)
                            .setTitle("⏳ Ожидайте")
                            .setDescription("Мы находимся в поиске психолога для вас...")
                            .setFooter("Спасибо за ваше терпение")
                            .setTimestamp(Instant.now());

                    textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
                    textChannel.sendMessage(member.getAsMention()).queue();
                    textChannel.sendMessageEmbeds(embedBuilder1.build()).queue();

                    if (textChannelAdmin != null) {  // Добавление кнопки
                        textChannelAdmin.sendMessage(role.getAsMention())
                                .setEmbeds(embedBuilder.build())
                                .setActionRow(
                                        Button.success("take-ticket:" + ticketNumber + ":" + ticketName, "Взять тикет")
                                                .withEmoji(Emoji.fromUnicode("\uD83D\uDCE5") // Add ticket ID to the button ID
                                                )).queue();
                    }
                    System.out.println("Пользователь " + member.getEffectiveName() + " создал тикет!");
                });
    }
}