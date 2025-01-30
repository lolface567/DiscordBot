package org.Psyholog;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.Psyholog.CheakPsyholog.CheckPsyhologCommand;
import org.Psyholog.CheakPsyholog.TopPsyhologCommand;
import org.Psyholog.DevCommands.*;
import org.Psyholog.Feedback.FeedBackCommand;
import org.Psyholog.Feedback.FeedBackSystem;
import org.Psyholog.Menu.MenuButtons;
import org.Psyholog.Menu.MenuCommandEx;
import org.Psyholog.Menu.MenuSystem;
import org.Psyholog.Ticket.*;
import org.Psyholog.voiceChanelCreator.VoiceInteract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.EnumSet;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Load environment variables
        Dotenv dotenv = Dotenv.configure()
                .load();

        // Define the intents for the bot
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.DIRECT_MESSAGE_POLLS,
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.GUILD_VOICE_STATES
        );

        // Build the JDA instance
        JDA jda = JDABuilder.createLight(dotenv.get("TOKEN"), intents)
                .setActivity(Activity.listening("Ваши проблемы"))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(
                        new CreateTicketSystemCommand(), new CreateAndSendTicket(), new CreateTicket(),
                        new MenuCommandEx(), new ClearOpenCommand(), new MenuButtons(),
                        new ReTakeTicketOnModal(), new ReTakeButtonInteract(), new FeedBackSystem(),
                        new ClearCloseCommand(), new CheckPsyhologCommand(), new ClearDescriptionCommand(),
                        new VoiceInteract(), new TopPsyhologCommand(), new BanUserLeavs(), new CheakBeforChanelDelete(),
                        new FeedBackCommand(), new MenuSystem(), new TakeTicketButton(), new TicketSystemMessage(),
                        new ClearKickedPsyholog(), new CahingRolle()
                )
                .build();
        logger.info("Bot Started!");
        logger.info("Version 1.5.7");

        // Add slash commands
        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("clear-ticket-des", "Чистит базу данных от описаний сохраненных тикетов")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("clear-baned-psyholog", "Чистит базу данных от снятых психологов")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("create-ticket-sys", "Создает тикет систему")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("rating", "Чекает средний бал психолога")
                        .addOption(OptionType.STRING,"name" , "Психолог"),
                Commands.slash("top", "Показывает топ психологов на сервере"),
                Commands.slash("menu", "Вызывает меню психолога"),
                Commands.slash("clear-open-tickets", "Удаляет из базы активные тикеты")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("clear-closed-tickets", "Удаляет все закрытые тикеты")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        );
        commands.queue();
    }
}