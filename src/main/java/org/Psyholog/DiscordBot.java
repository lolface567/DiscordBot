package org.Psyholog;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.Psyholog.check_psyholog.CheckPsyhologCommand;
import org.Psyholog.check_psyholog.TopPsyhologCommand;
import org.Psyholog.dev_commands.*;
import org.Psyholog.economyc.*;
import org.Psyholog.feedback.FeedBackCommand;
import org.Psyholog.feedback.FeedBackSystem;
import org.Psyholog.menu.MenuButtons;
import org.Psyholog.menu.MenuCommandEx;
import org.Psyholog.menu.MenuSystem;
import org.Psyholog.ticket.*;
import org.Psyholog.voice_channel_creator.VoiceInteract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiscordBot {
    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

    @Autowired
    public DiscordBot(Shop shop, Stats stats, TransferCoin transferCoin, EarnCoins earnCoins, Rewards rewards, CreateTicket createTicket, ReTakeButtonInteract reTakeButtonInteract,
    ReTakeTicketOnModal reTakeTicketOnModal, TakeTicketButton takeTicketButton, TicketSystemMessage ticketSystemMessage, CheckPsyhologCommand checkPsyhologCommand,
    TopPsyhologCommand topPsyhologCommand, BanUserLeavs banUserLeavs, CheakBeforChanelDelete cheakBeforChanelDelete, ClearCloseCommand clearCloseCommand,
                      ClearKickedPsyholog clearKickedPsyholog, TicketLogs ticketLogs, FeedBackSystem feedBackSystem, MenuButtons menuButtons, MenuCommandEx menuCommandEx) {
        // Load environment variables
        Dotenv dotenv = Dotenv.configure()
                .filename("settings.env")
                .load();

        // Build the JDA instance
        JDA jda = JDABuilder.createDefault(dotenv.get("TOKEN")) // Вместо createLight
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES) // Включаем голосовые события
                .enableIntents(GatewayIntent.GUILD_MESSAGES)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .enableIntents(GatewayIntent.GUILD_MEMBERS) // Кэшируем мемберов
                .setMemberCachePolicy(MemberCachePolicy.ALL) // Полное кэширование участников
                .enableCache(CacheFlag.VOICE_STATE) // Включаем кэширование голосовых каналов
                .addEventListeners(
                        new CreateTicketSystemCommand(), new CreateAndSendTicket(), createTicket,
                        menuCommandEx, menuButtons, reTakeTicketOnModal, reTakeButtonInteract,
                        feedBackSystem, clearCloseCommand, checkPsyhologCommand,
                        new VoiceInteract(), topPsyhologCommand, banUserLeavs, cheakBeforChanelDelete,
                        new FeedBackCommand(), new MenuSystem(), takeTicketButton, ticketSystemMessage,
                        clearKickedPsyholog, new CahingRolle(), ticketLogs,
                        new UserLogs(), shop, stats, transferCoin, earnCoins, rewards
                )
                .build();
        logger.info("Bot Started!");
        logger.info("Version 1.9.1");

        rewards.RewardScheduler(jda, dotenv.get("EARN_MONEY_CHANNEL_ID"));

        logger.info("DataBase successfully connected");

        // Add slash commands
        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("shop", "Открывает магазин сервера"),
                Commands.slash("give", "Перевести монеты")
                        .addOption(OptionType.STRING, "id_user", "Айди юзера")
                        .addOption(OptionType.STRING, "count", "Количество"),
                Commands.slash("check_coins", "Проверить количество монет")
                        .addOption(OptionType.STRING, "id_user", "Айди юзера"),
                Commands.slash("remove_role_from_shop", "Убрать роль с продажи")
                        .addOption(OptionType.STRING, "role_id", "Айди роли")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("add_role_to_shop", "Выставить роль на продажу")
                        .addOption(OptionType.STRING, "role_id", "Айди роли")
                        .addOption(OptionType.STRING, "role_cost", "Цена")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("stats", "Открывает вашу статистику на сервере"),
                Commands.slash("add_coins", "Прибавляет монеты юзеру")
                        .addOption(OptionType.STRING, "id", "Айди юзера")
                        .addOption(OptionType.STRING, "coins", "Количество")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("clear-baned-psyholog", "Чистит базу данных от снятых психологов")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("create-ticket-sys", "Создает тикет систему")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("rating", "Чекает средний бал психолога")
                        .addOption(OptionType.STRING, "name", "Психолог"),
                Commands.slash("top", "Показывает топ психологов на сервере"),
                Commands.slash("menu", "Вызывает меню психолога"),
                Commands.slash("clear-closed-tickets", "Удаляет все закрытые тикеты")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        );
        commands.queue();
    }
}
