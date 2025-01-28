package org.Psyholog.DevCommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Ticket.DataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearDescriptionCommand extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClearDescriptionCommand.class);
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if ("clear-ticket-des".equals(event.getName())) {
            Guild guild = event.getGuild();

            if (guild == null) {
                event.reply("Ошибка: гильдия не найдена.").setEphemeral(true).queue();
                return;
            }
            int bilo = DataStorage.getInstance().getTicketDes().size();

            DataStorage.getInstance().getTicketDes().clear();
            DataStorage.getInstance().saveData();

            int stalo = bilo - DataStorage.getInstance().getTicketDes().size();

            logger.info("Очистилось: " + stalo + " описаний");
            event.reply("Описание почищеные (наверное)").setEphemeral(true).queue();
        }
    }
}
