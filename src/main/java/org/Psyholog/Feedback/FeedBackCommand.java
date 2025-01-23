package org.Psyholog.Feedback;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class FeedBackCommand extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getButton().getId().startsWith("feedback:")) { // Check for feedback button with ticket ID
            Member member = event.getMember();
            String ticketId = event.getButton().getId().split(":")[1]; // Extract ticket ID from button ID
            TextInput descriptionInput = TextInput.create("body", "Отзыв", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Напишите Ваш отзыв")
                    .setMinLength(5)
                    .setMaxLength(1000)
                    .build();

            TextInput ratingInput = TextInput.create("steamId", "Оценка до 10 (Обязательно)", TextInputStyle.SHORT)
                    .setPlaceholder("Ваша оценка")
                    .build();

            Modal feedback = Modal.create("feedback:" + ticketId + ":" + member.getId(), "Напишите краткий отзыв") // Add ticket ID to modal ID
                    .addComponents(ActionRow.of(ratingInput), ActionRow.of(descriptionInput))
                    .build();

            event.replyModal(feedback).queue();
        }
    }
}
