package org.Psyholog.Ticket;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class ButtonMakeTicket extends ListenerAdapter {
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


            Modal modalBug = Modal.create("bug-report", "Вопрос к психологу")
                    .addComponents(ActionRow.of(type), ActionRow.of(age), ActionRow.of(DescriptionInput))
                    .build();

            event.replyModal(modalBug).queue();
        }
    }
    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("bug-report")) {
            String type = event.getValue("type").getAsString();
            String age = event.getValue("age").getAsString();
            String DescriptionInput = event.getValue("body").getAsString();
            CreateSys.execute(event, type, age, DescriptionInput);
            event.reply("Ваш запрос принят!").setEphemeral(true).queue();
        }
    }
}
