package org.Psyholog.economyc;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.service.UserCoinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TransferCoin extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TransferCoin.class);
    private final UserCoinService userCoinService;

    @Autowired
    public TransferCoin(UserCoinService userCoinService) {
        this.userCoinService = userCoinService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("give")) {
            User user = event.getUser();
            long id_user;

            if (event.getOption("id_user") != null) {
                id_user = Objects.requireNonNull(event.getOption("id_user")).getAsLong();
            } else {
                event.reply("Нужно передать id роли").setEphemeral(true).queue();
                logger.info("Пользователь не передал параметры для команды");
                return;
            }

            int count;
            if (event.getOption("count") != null) {
                count = Objects.requireNonNull(event.getOption("count")).getAsInt();
            } else {
                event.reply("Нужно передать количество которое вы хотите передать").setEphemeral(true).queue();
                logger.info("Пользователь не передал параметры для команды");
                return;
            }

            int countOfMemberCoins = userCoinService.getBalance(user.getIdLong());
            if (countOfMemberCoins >= count) {
                userCoinService.removeCoins(user.getIdLong(), count);
                count = count - (count / 100 * 5);
                userCoinService.addCoins(id_user, count);
                event.reply("Монеты успешно переведены").setEphemeral(true).queue();
            }
        }
    }
}
