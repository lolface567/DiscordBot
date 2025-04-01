package org.Psyholog.DevCommands;

import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.Main;
import org.Psyholog.Ticket.CreateTicket;
import org.Psyholog.Ticket.DataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.Psyholog.Menu.MenuButtons.userActiveVoiceMapMemory;
import static org.Psyholog.Menu.MenuButtons.userActiveVoiceMemory;

public class CheakBeforChanelDelete extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CheakBeforChanelDelete.class);
    @Override
    public void onChannelDelete(ChannelDeleteEvent event) { // Если пкмом удалить канал
        String textChannel = event.getChannel().getId();
        if (DataStorage.getInstance().getTicketIdName(textChannel) != null) {
            DataStorage.getInstance().closeTicket(textChannel);
            String user = DataStorage.getInstance().getUser(textChannel);
            CreateTicket.userActiveTicketsMemory.remove(user);
            DataStorage.getInstance().closeTicket(textChannel);
        }
        if (userActiveVoiceMapMemory.containsKey(textChannel)) {  // Если пкмом удалить войс созданый через меню
            String user = userActiveVoiceMapMemory.get(textChannel);
            userActiveVoiceMemory.remove(user);
            userActiveVoiceMapMemory.remove(textChannel);
            logger.info("Войс был успешно удален пкмом");
        }
    }
}
