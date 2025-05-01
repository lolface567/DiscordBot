package org.Psyholog.dev_commands;

import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.service.TicketsService;
import org.Psyholog.ticket.CreateTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.Psyholog.menu.MenuButtons.userActiveVoiceMapMemory;
import static org.Psyholog.menu.MenuButtons.userActiveVoiceMemory;

@Service
public class CheakBeforChanelDelete extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CheakBeforChanelDelete.class);

    private final TicketsService ticketsService;

    @Autowired
    public CheakBeforChanelDelete(TicketsService ticketsService) {
        this.ticketsService = ticketsService;
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) { // Если пкмом удалить канал
        String textChannel = event.getChannel().getId();
        if (ticketsService.getTicketByChannelId(textChannel).isPresent()) {
            ticketsService.closeTicket(Integer.parseInt(textChannel));
            String user = ticketsService.getUserIdByChannelId(textChannel);
            CreateTicket.userActiveTicketsMemory.remove(user);
            ticketsService.closeTicket(Integer.parseInt(textChannel));
        }
        if (userActiveVoiceMapMemory.containsKey(textChannel)) {  // Если пкмом удалить войс созданый через меню
            String user = userActiveVoiceMapMemory.get(textChannel);
            userActiveVoiceMemory.remove(user);
            userActiveVoiceMapMemory.remove(textChannel);
            logger.info("Войс был успешно удален пкмом");
        }
    }
}
