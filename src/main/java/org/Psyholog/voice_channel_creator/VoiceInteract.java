package org.Psyholog.voice_channel_creator;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.Psyholog.dev_commands.DotenvConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoiceInteract extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(VoiceInteract.class);
    private final Map<String, String> activeVoices = new HashMap<>();
    private static final DotenvConfig DOTENV_CONFIG = new DotenvConfig();
    private static final Dotenv DOTENV = DOTENV_CONFIG.dotenv();
    String voiceChannelId = DOTENV.get("PRIVATE_VOICE_ID");
    String categoryVoiceId = DOTENV.get("PRIVATE_VOICE_CATEGORY_ID");

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();

        if (event.getChannelJoined() != null) {
            AudioChannel joinedChannel = event.getChannelJoined(); // AudioChannel - общий тип для VoiceChannel и StageChannel

            if (joinedChannel instanceof VoiceChannel) { // Проверяем, является ли это голосовым каналом
                VoiceChannel voiceChannel = (VoiceChannel) joinedChannel;

                if (voiceChannel.getId().equals(voiceChannelId)) {
                    guild.createVoiceChannel("Приват " + member.getEffectiveName())
                            .setParent(guild.getCategoryById(categoryVoiceId))
                            .setUserlimit(2)
                            .queue(voiceChannel1 -> {
                                guild.moveVoiceMember(member, voiceChannel1).queue();
                                activeVoices.put(voiceChannel1.getId(), member.getId());

                                // Отправка сообщения в системный текстовый канал
                                VoiceChannel textChannel = guild.getVoiceChannelById(voiceChannel1.getId());
                                if (textChannel != null) {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setTitle("🎧 Ваш личный голосовой канал!")
                                            .setColor(Color.CYAN)
                                            .setDescription("Используйте кнопки ниже для управления вашим каналом. Вы можете изменить его название, установить лимит пользователей или кикнуть участника.")
                                            .setFooter("👑 Создатель: " + member.getEffectiveName(), null)
                                            .setTimestamp(Instant.now());

                                    textChannel.sendMessage(member.getAsMention() + ", ваш приватный канал создан! 🔥")
                                            .setEmbeds(embedBuilder.build())
                                            .addActionRow(
                                                    Button.primary("name:" + member.getId() + ":" + voiceChannel1.getId(), "✏ Изменить имя"),
                                                    Button.primary("limit:" + member.getId() + ":" + voiceChannel1.getId(), "📏 Установить лимит"),
                                                    Button.danger("kick:" + member.getId() + ":" + voiceChannel1.getId(), "⛔ Кикнуть участника"),
                                                    Button.primary("closeVoice:" + member.getId() + ":" + voiceChannel1.getId(), "🔒 Закрыть канал")
                                            )
                                            .queue();
                                }
                            });
                }
            }
        }

        // Проверяем, вышел ли кто-то из канала
        if (event.getChannelLeft() != null) {
            VoiceChannel leftChannel = event.getChannelLeft().asVoiceChannel();

            // Если канал пустой и хранится в activeVoices то он удалится
            if (leftChannel.getMembers().isEmpty() && activeVoices.containsKey(leftChannel.getId())) {
                leftChannel.delete().queue();
                activeVoices.remove(leftChannel.getId());
                logger.info("Удалён пустой приватный канал: " + leftChannel.getName());
            }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().startsWith("kick-select:")) {
            String[] parts = event.getComponentId().split(":");
            String ownerId = parts[1]; // ID создателя комнаты
            String voiceId = parts[2]; // ID голосового канала

            Member member = event.getMember();
            Guild guild = event.getGuild();

            if (member == null || guild == null) return;
            if (!member.getId().equals(ownerId)) {
                event.reply("❌ Только создатель комнаты может исключать участников.").setEphemeral(true).queue();
                return;
            }

            String selectedValue = event.getValues().get(0); // Получаем ID пользователя для кика
            String[] valueParts = selectedValue.split(":");
            String targetId = valueParts[2]; // ID пользователя

            Member targetMember = guild.getMemberById(targetId);
            if (targetMember == null) {
                event.reply("❌ Участник не найден.").setEphemeral(true).queue();
                return;
            }

            AudioChannel voiceChannel = targetMember.getVoiceState().getChannel();
            if (voiceChannel == null || !voiceChannel.getId().equals(voiceId)) {
                event.reply("❌ Участник уже покинул голосовой канал.").setEphemeral(true).queue();
                return;
            }

            // Кикаем пользователя, перемещая его в пустоту
            guild.kickVoiceMember(targetMember).queue(
                    success -> event.reply("✅ **" + targetMember.getEffectiveName() + "** был кикнут из голосового канала.").queue(),
                    failure -> event.reply("⚠️ Не удалось кикнуть пользователя: " + failure.getMessage()).setEphemeral(true).queue()
            );
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getButton().getId().startsWith("closeVoice:")) {
            String[] parts = event.getButton().getId().split(":");
            String ownerId = parts[1]; // ID создателя комнаты
            String voiceId = parts[2]; // ID голосового канала
            Member member = event.getMember();
            Guild guild = event.getGuild();

            if (member == null || guild == null) return;

            if (!member.getId().equals(ownerId)) {
                event.reply("❌ Только создатель комнаты может управлять доступом.").setEphemeral(true).queue();
                return;
            }

            VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceId);
            if (voiceChannel == null) {
                event.reply("❌ Голосовой канал не найден.").setEphemeral(true).queue();
                return;
            }

            // Закрываем канал для всех, кроме админов
            voiceChannel.getManager().putPermissionOverride(guild.getPublicRole(),
                    List.of(),
                    List.of(net.dv8tion.jda.api.Permission.VIEW_CHANNEL)).queue(
                    success -> {
                        event.reply("🔒 Канал теперь закрыт для всех, кроме администраторов!").setEphemeral(true).queue();
                        event.getMessage().editMessageComponents(ActionRow.of(
                                Button.primary("name:" + member.getId() + ":" + voiceChannel.getId(), "✏ Изменить имя"),
                                Button.primary("limit:" + member.getId() + ":" + voiceChannel.getId(), "📏 Установить лимит"),
                                Button.danger("kick:" + member.getId() + ":" + voiceChannel.getId(), "⛔ Кикнуть участника"),
                                Button.primary("openVoice:" + ownerId + ":" + voiceId, "🔓 Открыть канал")
                        )).queue();
                    },
                    error -> event.reply("❌ Не удалось изменить доступ: " + error.getMessage()).setEphemeral(true).queue()
            );
        }

        if (event.getButton().getId().startsWith("openVoice:")) {
            String[] parts = event.getButton().getId().split(":");
            String ownerId = parts[1]; // ID создателя комнаты
            String voiceId = parts[2]; // ID голосового канала
            Member member = event.getMember();
            Guild guild = event.getGuild();

            if (member == null || guild == null) return;

            if (!member.getId().equals(ownerId)) {
                event.reply("❌ Только создатель комнаты может управлять доступом.").setEphemeral(true).queue();
                return;
            }

            VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceId);
            if (voiceChannel == null) {
                event.reply("❌ Голосовой канал не найден.").setEphemeral(true).queue();
                return;
            }

            // Открываем канал для всех
            voiceChannel.getManager().putPermissionOverride(guild.getPublicRole(),
                    List.of(net.dv8tion.jda.api.Permission.VIEW_CHANNEL),
                    List.of()).queue(
                    success -> {
                        event.reply("🔓 Канал теперь открыт для всех!").setEphemeral(true).queue();
                        event.getMessage().editMessageComponents(ActionRow.of(
                                Button.primary("name:" + member.getId() + ":" + voiceChannel.getId(), "✏ Изменить имя"),
                                Button.primary("limit:" + member.getId() + ":" + voiceChannel.getId(), "📏 Установить лимит"),
                                Button.danger("kick:" + member.getId() + ":" + voiceChannel.getId(), "⛔ Кикнуть участника"),
                                Button.primary("closeVoice:" + ownerId + ":" + voiceId, "🔒 Закрыть канал")
                        )).queue();
                    },
                    error -> event.reply("❌ Не удалось изменить доступ: " + error.getMessage()).setEphemeral(true).queue()
            );
        }

        if (event.getButton().getId().startsWith("kick:")) {
            String[] parts = event.getButton().getId().split(":");
            String ownerId = parts[1]; // ID создателя комнаты
            String voiceId = parts[2]; // ID голосового канала
            Member member = event.getMember();
            Guild guild = event.getGuild();

            if (member == null || guild == null) return;

            if (!member.getId().equals(ownerId)) {
                event.reply("❌ Только создатель комнаты может кикать участников.").setEphemeral(true).queue();
                return;
            }

            VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceId);
            if (voiceChannel == null) {
                event.reply("❌ Голосовой канал не найден.").setEphemeral(true).queue();
                return;
            }

            java.util.List<Member> membersInVoice = voiceChannel.getMembers();

            if (membersInVoice.size() <= 1) {
                event.reply("👤 В голосовом канале больше никого нет.").setEphemeral(true).queue();
                return;
            }

            List<SelectOption> options = new ArrayList<>();

            for (Member m : membersInVoice) {
                if (!m.getId().equals(ownerId)) { // Не включаем владельца
                    options.add(SelectOption.of(m.getEffectiveName(), "kickuser:" + ownerId + ":" + m.getId() + ":" + voiceId)
                            .withDescription("Выгнать " + m.getEffectiveName())); // Добавляем описание
                }
            }

            String selectId = "kick-select:" + ownerId + ":" + voiceId;

            event.reply("🔽 **Выберите участника для кика**:")
                    .setEphemeral(true)
                    .addActionRow(StringSelectMenu.create(selectId)
                            .setPlaceholder("Выберите участника...")
                            .addOptions(options)
                            .build())
                    .queue();
        }

        if (event.getButton().getId().startsWith("name:")) {
            String[] parts = event.getButton().getId().split(":");
            String memberId = parts[1]; // Извлекаем ID тикета из имени
            String voiceId = parts[2]; // Извлекаем айди канала
            Member member = event.getMember();

            assert member != null;
            if (member.getId().equals(memberId)) {
                TextInput DescriptionInput = TextInput.create("voiceName", "Введите новое имя для канала", TextInputStyle.SHORT)
                        .setPlaceholder("Введите название...")
                        .setMinLength(1)
                        .setMaxLength(20)
                        .build();
                Modal voiceNameGive = Modal.create("changeNameModal:" + memberId + ":" + voiceId, "Изменение имени канала")
                        .addComponents(ActionRow.of(DescriptionInput))
                        .build();
                event.replyModal(voiceNameGive).queue();
            } else {
                event.reply("❌ Только создатель голосового канала может менять его название.").setEphemeral(true).queue();
            }
        }
        if (event.getButton().getId().startsWith("limit:")) {
            String[] parts = event.getButton().getId().split(":");
            String memberId = parts[1]; // Извлекаем ID тикета из имени
            String voiceId = parts[2]; // Извлекаем айди канала
            Member member = event.getMember();

            assert member != null;
            if (member.getId().equals(memberId)) {
                TextInput DescriptionInput = TextInput.create("voiceLimit", "Укажите лимит пользователей", TextInputStyle.SHORT)
                        .setPlaceholder("Введите число")
                        .setMinLength(1)
                        .setMaxLength(20)
                        .build();
                Modal voiceLimitGive = Modal.create("changeLimitModal:" + memberId + ":" + voiceId, "Изменение лимита пользователей")
                        .addComponents(ActionRow.of(DescriptionInput))
                        .build();
                event.replyModal(voiceLimitGive).queue();
            } else {
                event.reply("❌ Только создатель голосового канала может менять лимит.").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().startsWith("changeLimitModal:")) {
            Guild guild = event.getGuild();
            ModalMapping type = event.getValue("voiceLimit");
            String[] parts = event.getModalId().split(":");
            String voiceId = parts[2];
            assert type != null;
            String type2 = type.getAsString();
            assert guild != null;
            int range = 2;

            VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceId);
            assert voiceChannel != null;

            try {
                range = Integer.parseInt(type2);
            } catch (Exception e) {
                logger.error("Ошибка в VoiceInteract 324");
                voiceChannel.getManager().setUserLimit(2).queue(
                        success -> event.reply("❌ Некорректный ввод. Лимит установлен на " + 2).setEphemeral(true).queue(),
                        error -> event.reply("❌ Не удалось изменить лимит: " + error.getMessage()).setEphemeral(true).queue()
                );
                return;
            }

            int finalRange = range;

            if (finalRange > 99) {
                event.reply("Вы не можете установить лимит пользователей больше 99").setEphemeral(true).queue();
                return;
            }

            voiceChannel.getManager().setUserLimit(range).queue(
                    success -> event.reply("✅ Лимит пользователей изменён на " + finalRange).setEphemeral(true).queue(),
                    error -> event.reply("❌ Не удалось изменить лимит: " + error.getMessage()).setEphemeral(true).queue()
            );
        }
        if (event.getModalId().startsWith("changeNameModal:")) {
            Guild guild = event.getGuild();
            ModalMapping type = event.getValue("voiceName");
            String[] parts = event.getModalId().split(":");
            String voiceId = parts[2];

            try {
                assert guild != null;
                VoiceChannel voiceChannel = guild.getVoiceChannelById(voiceId);
                assert type != null;
                assert voiceChannel != null;
                voiceChannel.getManager().setName(type.getAsString()).queue(
                        success -> event.reply("✅ Имя канала изменено на **" + type.getAsString() + "**").setEphemeral(true).queue(),
                        error -> event.reply("❌ Не удалось изменить имя: " + error.getMessage()).setEphemeral(true).queue()
                );
            } catch (Exception e) {
                logger.error("Ошибка в VoiceInteract 360");
            }
        }
    }
}