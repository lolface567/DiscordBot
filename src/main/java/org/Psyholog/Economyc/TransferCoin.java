package org.Psyholog.Economyc;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TransferCoin extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TransferCoin.class);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
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

        int countOfMemberCoins = DatabaseManager.getBalance(user.getId());
        if (countOfMemberCoins >= count) {
            DatabaseManager.createUser(id_user);
            DatabaseManager.removeCoins(Long.parseLong(user.getId()), count);
            count = count - (count / 100 * 5);
            DatabaseManager.addCoins(id_user, count);
            event.reply("Монеты успешно переведены").setEphemeral(true).queue();
        }
    }
}
