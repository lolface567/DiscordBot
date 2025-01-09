package org.Psyholog.Ticket;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
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

import static org.Psyholog.Ticket.CreateSys.*;

public class MenuButtons extends ListenerAdapter {
    public static Set<String> userActiveVoiceMemory = new HashSet<>();
    public static Map<String, String> userActiveVoiceMapMemory = new HashMap<>();

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getButton().getId().startsWith("close-ticket:")) {
            String[] parts = event.getButton().getId().split(":");
            String ticketId = parts[1];
            String ticketName = parts[2];
            String[] cahnel = ticketName.split("-");
            String ticketIdname = cahnel[1];

            if (ticketId == null) {
                event.reply("Ошибка: тикет не найден.").setEphemeral(true).queue();
                return;
            }

            Member member = event.getMember();
            if (member == null) {
                event.reply("Ошибка: мембер не найден.").setEphemeral(true).queue();
            }

            Guild guild = event.getGuild();
            if (guild == null) {
                event.reply("Ошибка: гильдия не найдена.").setEphemeral(true).queue();
                return;
            }

            Role role = guild.getRoleById(psyhologRole);
            if (role == null) {
                event.reply("Ошибка: роль не найдена.").setEphemeral(true).queue();
                return;
            }

            if (member.getRoles().contains(role)) {
                if (DataStorage.getInstance().getClosedTickets().contains(ticketId)) {
                    event.getHook().sendMessage("Ошибка: тикет уже закрыт.").setEphemeral(true).queue();
                    return;
                }

                TextChannel textChannel = guild.getTextChannelById(ticketId);
                Category category = guild.getCategoryById(closeTicketCategory);
                String user = DataStorage.getInstance().getUserActiveTickets().get(ticketId);
                Member chel = guild.getMemberById(user);

                System.out.println(chel.getEffectiveName());

                if (textChannel != null) {
                    textChannel.getManager().setParent(category).queue(
                            success -> {
                                event.editComponents(
                                        ActionRow.of(
                                                Button.danger("closed-ticket", "Тикет закрыл: " + member.getEffectiveName()).withEmoji(Emoji.fromUnicode("\uD83D\uDD8D\uFE0F")).asDisabled()
                                        )).queue();
                            });
                    // Точка, где тикет помечается закрытым:
                    DataStorage.getInstance().getClosedTickets().add(ticketId);
                    DataStorage.getInstance().getTicketDes().remove(ticketIdname);
                    try {
                        CreateSys.userActiveTicketsMemory.remove(user);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        DataStorage.getInstance().saveData(); // Сохраняем данные в файл
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Данные сохранены");
                }else {
                    event.reply("Что то не так...").setEphemeral(true).queue();
                }

                if (chel != null) {
                    textChannel.upsertPermissionOverride(chel)
                            .setAllowed(Permission.VIEW_CHANNEL)
                            .setDenied(Permission.MESSAGE_SEND);
                } else {
                    System.out.println("Чеееееел не найден");
                }

                PermissionOverride rolePermissionOverride = textChannel.getPermissionOverride(role);

                if (rolePermissionOverride != null) {
                    // Если разрешение уже существует, обновите его
                    rolePermissionOverride.getManager()
                            .setAllowed(EnumSet.of(Permission.VIEW_CHANNEL))
                            .setDenied(EnumSet.of(Permission.MESSAGE_SEND))
                            .queue(
                                    success -> System.out.println("Права успешно обновлены."),
                                    error -> System.err.println("Ошибка при обновлении прав: " + error.getMessage())
                            );
                } else {
                    // Если разрешение не существует, создайте новое
                    textChannel.upsertPermissionOverride(role)
                            .setAllowed(EnumSet.of(Permission.VIEW_CHANNEL))
                            .setDenied(EnumSet.of(Permission.MESSAGE_SEND))
                            .queue(
                                    success -> System.out.println("Права успешно установлены."),
                                    error -> System.err.println("Ошибка при установке прав: " + error.getMessage())
                            );
                }

                DataStorage.getInstance().getUserActiveTickets().remove(textChannel.getId());
                DataStorage.getInstance().saveData();

                EmbedBuilder embedBuilder1 = new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("📛 Ваш тикет закрыт")
                        .setDescription("Тикет был успешно закрыт. Спасибо за ваше обращение!")
                        .addField("🆔 Ticket ID", ticketName, false)
                        .addField("👤 Закрыл", member.getAsMention(), false)
                        .setFooter("Закрыто", member.getUser().getAvatarUrl()) // Добавляем аватар пользователя в футер
                        .setTimestamp(Instant.now());
                textChannel.sendMessageEmbeds(embedBuilder1.build()).queue();

                EmbedBuilder embedBuilder2 = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("📝 Оставьте отзыв")
                        .setDescription("Чтобы поделиться своим мнением, нажмите на кнопку ниже.")
                        .setFooter("Спасибо за ваш отзыв!")
                        .setTimestamp(Instant.now());
                textChannel.sendMessageEmbeds(embedBuilder2.build()).addActionRow(
                        Button.success("feedback:" + ticketIdname, "Оставить отзыв")
                        .withEmoji(Emoji.fromUnicode("\uD83D\uDC8C"))).queue(); // Добавляем ID тикета к кнопке обратной связи

            } else event.reply("У вас нету прав").setEphemeral(true).queue();
        } else if (event.getButton().getId().startsWith("change:")) {
            try {
                String ticketName = event.getButton().getId().split(":")[1]; // Extract ticket ID from button ID
                String[] channel = ticketName.split("-");
                if (channel.length < 2) {
                    event.reply("Ошибка: Неправильный формат ID тикета.").setEphemeral(true).queue();
                    return;
                }

                String ticketIdname = channel[1];
                String parseId = DataStorage.getInstance().getTicketChannelMap().get(ticketIdname);

                if (parseId == null) {
                    event.reply("Ошибка: Тикет не найден.").setEphemeral(true).queue();
                    return;
                }

                Guild guild = event.getGuild();
                Member member = event.getMember();
                Role psyhologyRole = guild.getRoleById(CreateSys.psyhologRole);
                if (member.getRoles().contains(psyhologyRole)) {

                    TextInput descriptionInput = TextInput.create("why", "Причина смены", TextInputStyle.SHORT)
                            .setPlaceholder("Почему вы хотите передать тикет?")
                            .setMinLength(1)
                            .setMaxLength(1000)
                            .build();

                    Modal modalChange = Modal.create("changePsyhologModal:" + parseId + ":" + ticketIdname, "Напишите краткое описание") // Add ticket ID to modal ID
                            .addComponents(ActionRow.of(descriptionInput))
                            .build();

                    event.replyModal(modalChange).queue();
                } else {
                    event.reply("Ошибка: у вас нету прав").setEphemeral(true).queue();
                }
            } catch (Exception e) {
                event.reply("Произошла ошибка при обработке запроса.").setEphemeral(true).queue();
                e.printStackTrace();
            }
        } else if (event.getButton().getId().startsWith("voice:")) {
            Guild guild = event.getGuild();
            Role role = guild.getRoleById(psyhologRole);
            Member proverka = event.getMember();
            if (role == null) {
                event.reply("Ошибка: роль не найдена.").setEphemeral(true).queue();
                return;
            }
            String ticketName = event.getButton().getId().split(":")[1]; // Extract ticket ID from button ID
            String[] channel = ticketName.split("-");

            if (channel.length < 2) {
                event.reply("Ошибка: Неправильный формат ID тикета.").setEphemeral(true).queue();
                return;
            }

            String ticketIdname = channel[1];
            String ticketId = DataStorage.getInstance().getTicketChannelMap().get(ticketIdname);

            Member psyholog = guild.getMemberById(DataStorage.getInstance().getTicketPsychologists().get(ticketIdname));
            Member user = guild.getMemberById(DataStorage.getInstance().getUserActiveTickets().get(ticketId));

            if (proverka.getRoles().contains(role) && !userActiveVoiceMemory.contains(user.getId())) {
                Category voice = guild.getCategoryById(voiceCategory);
                VoiceChannel newVoiceChannel = guild.createVoiceChannel(user.getEffectiveName() + " И " + psyholog.getEffectiveName())
                        .setParent(voice)
                        .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT))
                        .addPermissionOverride(psyholog, EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT), null)
                        .addPermissionOverride(user, EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT), null)
                        .complete();
                userActiveVoiceMemory.add(user.getId());
                userActiveVoiceMapMemory.put(newVoiceChannel.getId(), user.getId());

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder
                        .setTitle("🆕 Создан новый голосовой канал")
                        .setColor(Color.DARK_GRAY)
                        .setDescription("Это меню управления голосовым каналом. Используйте кнопки ниже для управления.")
                        .setFooter("Войс для " + user.getEffectiveName() + " и " + psyholog.getEffectiveName(), null)
                        .setTimestamp(Instant.now());

                newVoiceChannel.sendMessage(user.getAsMention() + " " + psyholog.getAsMention())
                        .setEmbeds(embedBuilder.build())
                        .setActionRow(
                                Button.danger("delvoice:" + newVoiceChannel.getId() + ":" + user.getId(), "Удалить войс")
                                        .withEmoji(Emoji.fromUnicode("🗑"))
                        )
                        .queue();

                event.reply("Войс создан").setEphemeral(true).queue();
            } else {
                event.reply("Ошибка: у вас уже создан канал с этим человеком или нету прав на выполнение этой комманды").setEphemeral(true).queue();
            }
        } else if (event.getButton().getId().startsWith("delvoice:")) {
            String voiceId = event.getButton().getId().split(":")[1]; // Extract ticket ID from button ID
            String user = event.getButton().getId().split(":")[2]; // Extract ticket ID from button ID

            Guild guild = event.getGuild();

            assert guild != null;
            userActiveVoiceMemory.remove(user);
            userActiveVoiceMapMemory.remove(voiceId);
            guild.getVoiceChannelById(voiceId).delete().queue();
        }
    }
}
