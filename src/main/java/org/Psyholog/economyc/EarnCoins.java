package org.Psyholog.economyc;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.Psyholog.dev_commands.DotenvConfig;
import org.Psyholog.service.UserCoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EarnCoins extends ListenerAdapter {
    private static final DotenvConfig DOTENV_CONFIG = new DotenvConfig();
    private static final Dotenv DOTENV = DOTENV_CONFIG.dotenv();
    private static final String ChannelForSpam = DOTENV.get("EARN_MONEY_CHANNEL_ID");
    private static final String systemMessages = DOTENV.get("SYSTEM_CHANNEL_ID");
    private final Map<Long, Long> voiceJoinTimes = new HashMap<>();
    private final UserCoinService userCoinService;

    @Autowired
    public EarnCoins(UserCoinService userCoinService) {
        this.userCoinService = userCoinService;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getChannel() instanceof TextChannel) { // Проверяем, является ли канал текстовым
            TextChannel eventChannel = (TextChannel) event.getChannel();
            Guild guild = event.getGuild();
            Member member = event.getMember();
            TextChannel coinChannel = guild.getTextChannelById(ChannelForSpam);

            if (member != null && !member.getUser().isBot() && coinChannel != null) {
                if (coinChannel.getId().equals(eventChannel.getId())) {
                    userCoinService.addCoins(member.getIdLong(), 2); // Добавляем 2 коина
                }
            }
        } else {
            System.out.println("Попытка использовать текстовые функции в нетекстовом канале!");
        }
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        long userId = event.getMember().getIdLong();
        Guild guild = event.getGuild();
        long currentTime = System.currentTimeMillis();

        // Пользователь покинул голосовой канал (или сменил канал)
        if (event.getChannelLeft() != null) {
            if (voiceJoinTimes.containsKey(userId)) {
                long joinTime = voiceJoinTimes.remove(userId); // Получаем время входа и удаляем запись
                long timeSpentMillis = currentTime - joinTime;
                int minutesSpent = (int) (timeSpentMillis / 60000); // Конвертация в минуты

                if (minutesSpent > 0) { // Начисляем монеты только если в голосе был хотя бы 1 минуту
                    int earnedCoins = minutesSpent * 3;
                    userCoinService.addCoins(userId, earnedCoins);

                    TextChannel logChannel = guild.getTextChannelById(systemMessages);
                    if (logChannel != null) {
                        logChannel.sendMessage(event.getMember().getEffectiveName() +
                                " получил " + earnedCoins + " монет за " + minutesSpent +
                                " минут в голосовом чате!").queue();
                    }
                }
            }
        }

        // Пользователь зашел в голосовой канал
        if (event.getChannelJoined() != null) {
            voiceJoinTimes.put(userId, currentTime); // Фиксируем время входа
        }
    }
}