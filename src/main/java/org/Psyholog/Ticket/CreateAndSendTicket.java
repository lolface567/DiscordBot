package org.Psyholog.Ticket;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class CreateAndSendTicket extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("ticket")) {
            TextInput DescriptionInput = TextInput.create("body", "Описание проблемы", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Краткое описания вашей проблемы")
                    .setMinLength(10)
                    .setMaxLength(1000)
                    .build();

            TextInput type = TextInput.create("type", "Тип общения", TextInputStyle.SHORT)
                    .setPlaceholder("Голосовой/текстовый")
                    .build();

            TextInput age = TextInput.create("age", "Возраст", TextInputStyle.SHORT)
                    .setPlaceholder("Ваш возраст")
                    .build();

            TextInput time = TextInput.create("timeZone", "Ваш часовой пояс", TextInputStyle.SHORT)
                    .setPlaceholder("Ваш часовой пояс")
                    .build();


            Modal ticketCreate = Modal.create("ticket-create", "Вопрос к психологу")
                    .addComponents(ActionRow.of(type), ActionRow.of(age), ActionRow.of(time), ActionRow.of(DescriptionInput))
                    .build();

            event.replyModal(ticketCreate).queue();
        }
    }
    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("ticket-create")) {
            String type = event.getValue("type").getAsString();
            String age = event.getValue("age").getAsString();
            String DescriptionInput = event.getValue("body").getAsString();
            String timeZone = event.getValue("timeZone").getAsString();
            CreateTicket.execute(event, type, age, DescriptionInput, timeZone);
            event.reply("Ваш запрос принят!").setEphemeral(true).queue();
        }
    }
}
