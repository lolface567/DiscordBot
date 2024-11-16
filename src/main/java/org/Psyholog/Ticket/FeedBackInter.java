package org.Psyholog.Ticket;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.Psyholog.Ticket.CreateSys.feedbackChannel;

public class FeedBackInter extends ListenerAdapter {
    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().startsWith("feedback:")) { // Check for feedback modal with ticket ID
            String ticketId = event.getModalId().split(":")[1]; // Extract ticket ID from modal ID
            String rating = event.getValue("steamId").getAsString();
            String feedbackText = event.getValue("body").getAsString();

            Guild guild = event.getGuild();
            if (guild == null) {
                event.reply("Ошибка: гильдия не найдена.").setEphemeral(true).queue();
                return;
            }
            TextChannel feedBackChannel = guild.getTextChannelById(feedbackChannel);
            if (feedBackChannel == null) {
                event.reply("Ошибка: канал для отзывов не найден.").setEphemeral(true).queue();
                return;
            }

            Member member = event.getMember();
            if (member == null) {
                event.reply("Ошибка: мембер не найден.").setEphemeral(true).queue();
                return;
            }

            Member psychologist = guild.getMemberById(DataStorage.getInstance().getTicketPsychologists().get(ticketId));// Get psychologist for the ticket

            if (member.getId().equals(psychologist.getId())) {
                event.reply("Ты психолог этого тикета").setEphemeral(true).queue();
                return;
            }

            if (rating.equals("1") || rating.equals("2") || rating.equals("3") || rating.equals("4") || rating.equals("5") ||
                    rating.equals("6") || rating.equals("7") || rating.equals("8") || rating.equals("9") || rating.equals("10")) {

                DecimalFormat df = new DecimalFormat("#.##");

                int ratingFormat = Integer.parseInt(rating);
                DataStorage.getInstance().addPsychologistRating(psychologist.getId(), Integer.parseInt(df.format(ratingFormat)));
                DataStorage.getInstance().saveData();
            }

            String averageRating = String.format("%.2f", DataStorage.getInstance().getAverageRating(psychologist.getId()));

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle(":sparkles:Новый отзыв!")
                    .setThumbnail(member.getUser().getAvatarUrl()) // Добавить миниатюру с аватаром пользователя
                    .addField(":bust_in_silhouette:Отзыв оставил: ", member.getAsMention(), false)
                    .addField(":id:Ticket ID", ticketId, false)
                    .addField(":star:Оценка", rating + " из 10", false) // Добавить информацию об оценке
                    .addField(":memo:Отзыв", feedbackText, false)
                    .addField(":busts_in_silhouette:Психолог", psychologist.getAsMention(), false) // Добавить имя психолога
                    .setFooter("Ваш средний бал " + averageRating, member.getUser().getAvatarUrl()) // Добавить футер с аватаром пользователя
                    .setTimestamp(Instant.now());
            if (rating.equals("1") || rating.equals("2") || rating.equals("3") || rating.equals("4") || rating.equals("5")) {
                embedBuilder.setColor(Color.red);
                embedBuilder.setTitle(":x:Новый негативный отзыв(");
            }

            feedBackChannel.sendMessageEmbeds(embedBuilder.build()).queue();

            // Update the feedback button to show that feedback has been submitted
            String channelId = DataStorage.getInstance().getTicketChannelMap().get(ticketId);
            if (channelId != null) {
                TextChannel ticketChannel = guild.getTextChannelById(channelId);
                if (ticketChannel != null) {
                    ticketChannel.retrieveMessageById(event.getMessage().getId()).queue(message -> {
                        java.util.List<ActionRow> actionRows = new ArrayList<>();
                        for (ActionRow actionRow : message.getActionRows()) {
                            List<net.dv8tion.jda.api.interactions.components.buttons.Button> updatedButtons = new ArrayList<>();
                            for (net.dv8tion.jda.api.interactions.components.buttons.Button button : actionRow.getButtons()) {
                                if (button.getId() != null && button.getId().startsWith("feedback:")) {
                                    event.reply("Спасибо за ваш отзыв!").setEphemeral(true).queue();
                                    updatedButtons.add(Button.danger("feedback-submitted", "Отзыв отправлен").withEmoji(Emoji.fromUnicode("\uD83D\uDC9A")).asDisabled());
                                } else {
                                    updatedButtons.add(button);
                                }
                            }
                            actionRows.add(ActionRow.of(updatedButtons));
                        }
                        message.editMessageComponents(actionRows).queue();
                    });
                }
            }
        }
    }
}
