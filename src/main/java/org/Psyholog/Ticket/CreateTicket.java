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
import org.Psyholog.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CreateTicket extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CreateTicket.class);

    public static Map<String, String> userActiveTicketsMemory = new HashMap<>();
    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static final String FEEDBACK_CHANNEL = Dotenv.load().get("feedbackChannel");
    public static final String ADMIN_CHANNEL = Dotenv.load().get("adminChannel");
    public static final String TICKET_CATEGORY = Dotenv.load().get("ticketCategory");
    public static final String PSYCHOLOGY_ROLE = Dotenv.load().get("psyhologRole");
    public static final String CLOSE_TICKET_CATEGORY = Dotenv.load().get("closeTicketCategory");
    public static final String VOICE_CATEGORY = Dotenv.load().get("voiceCategory");


    public static void execute(ModalInteractionEvent event, String type, String age, String descriptionInput, String timeZone) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Ошибка: гильдия не найдена.").setEphemeral(true).queue();
            return;
        }

        Category category = guild.getCategoryById(TICKET_CATEGORY);
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

        String ticketNumber = String.valueOf(DataStorage.getInstance().getNextTicketNumber());
        String ticketName = "ticket-" + ticketNumber;

        guild.createTextChannel(ticketName, category)
                .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .queue(textChannel -> {

                    userActiveTicketsMemory.put(member.getId(), textChannel.getId()); // добавляет юзера в бан лист
                    DataStorage.getInstance().addTicket(Integer.parseInt(ticketNumber), member.getId(), textChannel.getId(), descriptionInput);

                    TextChannel textChannelAdmin = guild.getTextChannelById(ADMIN_CHANNEL);

                    Role role = guild.getRoleById(PSYCHOLOGY_ROLE);

                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle("🆕 Новое обращение")
                            .setColor(Color.CYAN)
                            .setDescription("Поступило новое обращение. Подробности ниже:")
                            .addField("📂 Тип:", type, false)
                            .addField("🎂 Возраст:", age, false)
                            .addField("📝 Описание проблемы:", DataStorage.getInstance().getTicketDescription(Integer.parseInt(ticketNumber)), false)
                            .addField("\uD83D\uDD5D Часовой пояс:", timeZone, false)
                            .addField("📄 Ticket ID", ticketName, false)
                            .setFooter("Сообщение от " + member.getEffectiveName(), member.getUser().getAvatarUrl())
                            .setTimestamp(Instant.now());

                    EmbedBuilder embedBuilder1 = new EmbedBuilder()
                            .setColor(Color.CYAN)
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
                                                .withEmoji(Emoji.fromUnicode("\uD83D\uDCE5")
                                                )).queue();
                    }
                    logger.info("Пользователь " + member.getEffectiveName() + " создал тикет!");
                });
    }
}