package org.Psyholog;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.Psyholog.CheakPsyholog.CheckPsyhologCommand;
import org.Psyholog.CheakPsyholog.TopPsyhologCommand;
import org.Psyholog.DevCommands.*;
import org.Psyholog.Economyc.DatabaseManager;
import org.Psyholog.Economyc.EarnCoins;
import org.Psyholog.Economyc.Shop;
import org.Psyholog.Economyc.Stats;
import org.Psyholog.Feedback.FeedBackCommand;
import org.Psyholog.Feedback.FeedBackSystem;
import org.Psyholog.Menu.MenuButtons;
import org.Psyholog.Menu.MenuCommandEx;
import org.Psyholog.Menu.MenuSystem;
import org.Psyholog.Ticket.*;
import org.Psyholog.voiceChanelCreator.VoiceInteract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Load environment variables
        Dotenv dotenv = Dotenv.configure()
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
                        new CreateTicketSystemCommand(), new CreateAndSendTicket(), new CreateTicket(),
                        new MenuCommandEx(), new MenuButtons(), new ReTakeTicketOnModal(), new ReTakeButtonInteract(),
                        new FeedBackSystem(), new ClearCloseCommand(), new CheckPsyhologCommand(), new Shop(),
                        new VoiceInteract(), new TopPsyhologCommand(), new BanUserLeavs(), new CheakBeforChanelDelete(),
                        new FeedBackCommand(), new MenuSystem(), new TakeTicketButton(), new TicketSystemMessage(),
                        new ClearKickedPsyholog(), new CahingRolle(), new LogsSender(), new Stats(), new EarnCoins()
                )
                .build();
        logger.info("Bot Started!");
        logger.info("Version 1.8.1");

        DatabaseManager.initializeDatabase();
        DataStorage.getInstance();

        logger.info("DataBase successfully connected");

        // Add slash commands
        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("shop", "Открывает магазин сервера"),
                Commands.slash("add_role_to_shop", "Выставить роль на продажу")
                        .addOption(OptionType.STRING,"role_id" , "Айди роли")
                        .addOption(OptionType.STRING,"role_cost" , "Цена")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("stats", "Открывает вашу статистику на сервере"),
                Commands.slash("add_coins", "Прибавляет монеты юзеру")
                        .addOption(OptionType.STRING,"id" , "Айди юзера")
                        .addOption(OptionType.STRING,"coins" , "Количество")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("clear-baned-psyholog", "Чистит базу данных от снятых психологов")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("create-ticket-sys", "Создает тикет систему")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("rating", "Чекает средний бал психолога")
                        .addOption(OptionType.STRING,"name" , "Психолог"),
                Commands.slash("top", "Показывает топ психологов на сервере"),
                Commands.slash("menu", "Вызывает меню психолога"),
                Commands.slash("clear-closed-tickets", "Удаляет все закрытые тикеты")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        );
        commands.queue();
    }
}