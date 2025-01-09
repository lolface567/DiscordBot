package org.Psyholog.Ticket;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;


import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.Psyholog.Ticket.MenuButtons.userActiveVoiceMapMemory;
import static org.Psyholog.Ticket.MenuButtons.userActiveVoiceMemory;

public class CreateSys extends ListenerAdapter {

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
                });
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) { // Если пкмом удалить канал
        String textChannel = event.getChannel().getId();
        if (DataStorage.getInstance().getTicketChannelMap().containsValue(textChannel)) {
            DataStorage.getInstance().getClosedTickets().add(textChannel);
            String user = DataStorage.getInstance().getUserActiveTickets().get(textChannel);
            CreateSys.userActiveTicketsMemory.remove(user);
            DataStorage.getInstance().getTicketPsychologists().remove(textChannel);
            DataStorage.getInstance().getTicketChannelMap().remove(textChannel);
            DataStorage.getInstance().getUserActiveTickets().remove(textChannel);
            DataStorage.getInstance().saveData();
        }
        if (userActiveVoiceMapMemory.containsKey(textChannel)) {  // Если пкмом удалить войс созданый через меню
            String user = userActiveVoiceMapMemory.get(textChannel);
            userActiveVoiceMemory.remove(user);
            userActiveVoiceMapMemory.remove(textChannel);
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) { // Если чубрик ливнул с активным тикетом НУЖНО ПРОВЕРИТЬ ЭТОТ КОД
        Member leavMember = event.getMember();  // Получаем пользователя

        if (leavMember == null) {
            System.out.println("Member object is null, possibly due to member already leaving the server.");
            return; // Выход, если нет информации о пользователе
        }

        String stringMember = leavMember.getId();
        Guild guild = event.getGuild();

        // Проверяем, есть ли активные тикеты у пользователя
        if (DataStorage.getInstance().getUserActiveTickets().containsValue(stringMember)) {
            TextChannel textChannel = guild.getTextChannelById(userActiveTicketsMemory.get(stringMember));

            if (textChannel != null) { // Проверяем, что канал не null
                EmbedBuilder embedBuilder1 = new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("\uD83D\uDCA2 Пользователь покинул сервер")
                        .setDescription("Можно закрыть тикет, пользователь покинул сервер")
                        .setFooter("Фарту масти")
                        .setTimestamp(Instant.now());

                textChannel.sendMessageEmbeds(embedBuilder1.build()).queue();

                // Удаляем активный тикет пользователя
                DataStorage.getInstance().getUserActiveTickets().remove(textChannel.getId());
                userActiveTicketsMemory.remove(stringMember);
            } else {
                System.out.println("Текстовый канал не найден.");
            }
        }
        // Баним пользователя на 10 дней
        guild.ban(leavMember, 0, TimeUnit.DAYS).queue(
                success -> System.out.println("Юзер забанен"),
                error -> System.err.println("Не удалось забанить пользователя: " + error.getMessage())
        );
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {   // Обработка кнопки взять тикет
        if (event.getButton().getId().startsWith("take-ticket:")) {
            String[] parts = event.getButton().getId().split(":");
            String ticketNumber = parts[1];
            String ticketName = parts[2];
            String ticketId = DataStorage.getInstance().getTicketChannelMap().get(ticketNumber);


            Guild guild = event.getGuild();
            if (guild == null) {
                event.reply("Ошибка: гильдия не найдена.").setEphemeral(true).queue();
                return;
            }

            if (DataStorage.getInstance().getClosedTickets().contains(ticketId)) {
                event.reply("Ошибка: тикет уже закрыт").setEphemeral(true).queue();
                return;
            }

            Member member = event.getMember();
            if (member == null) {
                event.reply("Ошибка: участник не найден.").setEphemeral(true).queue();
                return;
            }

            System.out.println(member.getId() + " взял тикет " + ticketId);

            TextChannel textChannel = guild.getTextChannelById(ticketId);
            Member user = guild.getMemberById(DataStorage.getInstance().getUserActiveTickets().get(ticketId));

            if (user == null) {
                event.editComponents(
                        ActionRow.of(
                                Button.danger("taken-ticket", "Тикет был закрыт, юзер не найден").asDisabled().withEmoji(Emoji.fromUnicode("❌")),
                                Button.link(textChannel.getJumpUrl(), "Перейти к тикету")
                        )
                ).queue();
                scheduler.schedule(() -> {
                    event.getMessage().delete().queue();
                }, 10, TimeUnit.MINUTES);

                DataStorage.getInstance().getClosedTickets().add(ticketId);
                userActiveTicketsMemory.remove(user);
                DataStorage.getInstance().getUserActiveTickets().remove(textChannel.getId());
                DataStorage.getInstance().getTicketDes().remove(ticketId);
                DataStorage.getInstance().saveData();

                textChannel.delete().queue();
                return;
            }

            if (textChannel != null) {
                String newChannelName = ticketName + "-" + member.getEffectiveName();
                textChannel.getManager().setName(newChannelName).queue(
                        success -> {
                            event.editComponents(
                                    ActionRow.of(
                                            Button.danger("taken-ticket", "Взял: " + member.getEffectiveName()).asDisabled()
                                                    .withEmoji(Emoji.fromUnicode("✅")),
                                            Button.link(textChannel.getJumpUrl(), "Перейти к тикету")
                                    )
                            ).queue();

                            scheduler.schedule(() -> {
                                event.getMessage().delete().queue();
                            }, 10, TimeUnit.MINUTES);
                        },
                        error -> event.reply("Ошибка при обновлении имени канала: " + error.getMessage()).setEphemeral(true).queue()
                );

                Role psychologistRole = guild.getRoleById(CreateSys.psyhologRole);
                if (psychologistRole == null) {
                    event.reply("Ошибка: роль психолога не найдена.").setEphemeral(true).queue();
                    return;
                }

                DataStorage.getInstance().getTicketPsychologists().put(ticketNumber, member.getId());
                DataStorage.getInstance().saveData();

                // Получаем текущие разрешения для участника
                PermissionOverride existingPermission = textChannel.getPermissionOverride(member);

                if (existingPermission != null) {
                    // Если разрешение уже существует, обновляем его
                    existingPermission.getManager()
                            .grant(EnumSet.of(Permission.VIEW_CHANNEL))
                            .queue(
                                    success -> System.out.println("Права для участника успешно обновлены."),
                                    error -> System.err.println("Ошибка при обновлении прав для участника: " + error.getMessage())
                            );
                } else {
                    // Если разрешение не существует, создаем новое
                    textChannel.upsertPermissionOverride(member)
                            .setAllowed(EnumSet.of(Permission.VIEW_CHANNEL))
                            .queue(
                                    success -> System.out.println("Права для участника успешно установлены."),
                                    error -> System.err.println("Ошибка при установке прав для участника: " + error.getMessage())
                            );
                }

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.DARK_GRAY)
                        .setTitle("🎉 Психолог найден!")
                        .setDescription("Ваш психолог: " + member.getAsMention())
                        .addField("✨ Поддержка доступна", "Вы можете начать обсуждение.", false)
                        .setFooter("Мы здесь, чтобы помочь вам!")
                        .setTimestamp(Instant.now());
                textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            } else {
                event.reply("Ошибка: канал не найден.").setEphemeral(true).queue();
            }
        }
        if (event.getButton().getId().startsWith("feedback:")) { // Check for feedback button with ticket ID
            String ticketId = event.getButton().getId().split(":")[1]; // Extract ticket ID from button ID
            TextInput descriptionInput = TextInput.create("body", "Отзыв", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Напишите Ваш отзыв")
                    .setMinLength(5)
                    .setMaxLength(1000)
                    .build();

            TextInput ratingInput = TextInput.create("steamId", "Оценка до 10", TextInputStyle.SHORT)
                    .setPlaceholder("Ваша оценка")
                    .build();

            Modal modalBug = Modal.create("feedback:" + ticketId, "Напишите краткий отзыв") // Add ticket ID to modal ID
                    .addComponents(ActionRow.of(ratingInput), ActionRow.of(descriptionInput))
                    .build();

            event.replyModal(modalBug).queue();
        }
    }
}