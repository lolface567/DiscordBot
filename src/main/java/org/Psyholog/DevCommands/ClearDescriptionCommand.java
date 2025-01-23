package org.Psyholog.DevCommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Ticket.DataStorage;

public class ClearDescriptionCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if ("clear-ticket-des".equals(event.getName())) {
            Guild guild = event.getGuild();

            if (guild == null) {
                event.reply("Ошибка: гильдия не найдена.").setEphemeral(true).queue();
                return;
            }
            System.out.println(DataStorage.getInstance().getTicketDes().size() + " описаний всего");

            DataStorage.getInstance().getTicketDes().clear();
            DataStorage.getInstance().saveData();

            System.out.println("После чистки " + DataStorage.getInstance().getTicketDes().size());
            event.reply("Описание почищеные (наверное)").setEphemeral(true).queue();
        }
    }
}
