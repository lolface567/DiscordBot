package org.Psyholog.economyc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.Psyholog.service.ShopRolesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.Psyholog.service.UserCoinService;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class Shop extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Stats.class);
    private final UserCoinService userCoinService;
    private final ShopRolesService shopRolesService;

    @Autowired
    public Shop(UserCoinService userCoinService, ShopRolesService shopRolesService) {
        this.userCoinService = userCoinService;
        this.shopRolesService = shopRolesService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("shop")) {
            Member member = event.getMember();
            Guild guild = event.getGuild();
            Map<Long, Integer> roles = shopRolesService.getAllRolesWithPrices(); // Получаем список ролей и их цен

            if (roles.isEmpty()) {
                event.reply("В магазине пока нет товаров.").setEphemeral(true).queue();
                return;
            }

            assert member != null;
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("🛒 Магазин сервера")
                    .setColor(Color.CYAN)
                    .addField("\uD83D\uDCB0Ваш баланс: ", String.valueOf(userCoinService.getBalance(member.getIdLong())), false)
                    .setDescription("Выберите роль из списка ниже, а затем нажмите 'Купить'.");

            // Создаем выпадающее меню с ролями
            StringSelectMenu.Builder menu = StringSelectMenu.create("shop:select")
                    .setPlaceholder("Выберите роль для покупки");

            for (Map.Entry<Long, Integer> entry : roles.entrySet()) {
                assert guild != null;
                Role role = guild.getRoleById(entry.getKey());
                if (role != null) {
                    menu.addOption(role.getName() + " - " + entry.getValue() + " монет", role.getId());
                }
            }

            // Кнопка покупки
            Button buyButton = Button.success("shop:buy", "Купить");

            event.replyEmbeds(embedBuilder.build())
                    .addActionRow(menu.build()) // Добавляем выпадающий список
                    .addActionRow(buyButton)   // Добавляем кнопку "Купить"
                    .setEphemeral(true)
                    .queue();
        }

        if (event.getName().equals("add_role_to_shop")) {
            long role_id;
            if (event.getOption("role_id") != null) {
                role_id = Objects.requireNonNull(event.getOption("role_id")).getAsLong();
            } else {
                event.reply("Нужно передать id роли").setEphemeral(true).queue();
                logger.info("Пользователь не передал параметры для команды");
                return;
            }
            int role_cost;
            if (event.getOption("role_cost") != null) {
                role_cost = Objects.requireNonNull(event.getOption("role_cost")).getAsInt();
            } else {
                event.reply("Нужно передать цену роли").setEphemeral(true).queue();
                logger.info("Пользователь не передал параметры для команды");
                return;
            }
            shopRolesService.addRole(role_id, role_cost);
            event.reply("Роль: добавлена").setEphemeral(true).queue();
        }

        if (event.getName().equals("remove_role_from_shop")) {
            long role_id;
            if (event.getOption("role_id") != null) {
                role_id = Objects.requireNonNull(event.getOption("role_id")).getAsLong();
            } else {
                event.reply("Нужно передать id роли").setEphemeral(true).queue();
                logger.info("Пользователь не передал параметры для команды");
                return;
            }

            shopRolesService.deleteRole(role_id);
            event.reply("Роль удалена из магазина").setEphemeral(true).queue();
        }

        if (event.getName().equals("check_coins")) {
            long id_user;
            if (event.getOption("id_user") != null) {
                id_user = Objects.requireNonNull(event.getOption("id_user")).getAsLong();
            } else {
                event.reply("Нужно передать id роли").setEphemeral(true).queue();
                logger.info("Пользователь не передал параметры для команды");
                return;
            }
            Guild guild = event.getGuild();
            assert guild != null;
            Member member = guild.getMemberById(id_user);

            assert member != null;
            EmbedBuilder embed = new EmbedBuilder()
                    .setAuthor("\uD83D\uDD39" + member.getEffectiveName(), null)
                    .setColor(Color.CYAN) // Цвет рамки
                    .addField("💰Монеты:", String.valueOf(userCoinService.getBalance(member.getIdLong())), false)
                    .setThumbnail(member.getEffectiveAvatarUrl())
                    .setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("shop:select")) {
            String selectedRoleId = event.getValues().get(0);
            Role selectedRole = Objects.requireNonNull(event.getGuild()).getRoleById(selectedRoleId);

            if (selectedRole != null) {
                // Сохраняем выбор пользователя
                ShopPurchaseManager.setUserSelectedRole(event.getUser().getIdLong(), selectedRoleId);
            }

            // Подтверждаем интеракцию без ответа
            event.deferEdit().queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("shop:buy")) {
            long userId = event.getUser().getIdLong();
            String selectedRoleId = ShopPurchaseManager.getUserSelectedRole(userId);
            Member member = Objects.requireNonNull(event.getGuild()).getMember(event.getUser());

            if (selectedRoleId == null) {
                event.reply("Вы не выбрали роль!").setEphemeral(true).queue();
                return;
            }

            Role role = Objects.requireNonNull(event.getGuild()).getRoleById(selectedRoleId);
            if (role == null) {
                event.reply("Ошибка! Роль не найдена.").setEphemeral(true).queue();
                return;
            }

            int roleCost = shopRolesService.getRolePriceById(Long.valueOf(selectedRoleId));
            int userBalance = userCoinService.getBalance(userId);

            if (userBalance < roleCost) {
                event.reply("Недостаточно средств! Ваш баланс: " + userBalance + " монет.").setEphemeral(true).queue();
                return;
            }

            if (member.getRoles().contains(role)) {
                event.reply("У вас уже есть роль: " + role.getName()).setEphemeral(true).queue();
                return;
            }

            // Списываем монеты и выдаем роль
            userCoinService.removeCoins(userId, roleCost);
            event.getGuild().addRoleToMember(member, role).queue();

            event.reply("Поздравляем! Вы купили роль **" + role.getName() + "** за " + roleCost + " монет! 🎉")
                    .setEphemeral(true)
                    .queue();
        }
    }

    public static class ShopPurchaseManager {
        private static final Map<Long, String> userSelectedRoles = new HashMap<>();

        public static void setUserSelectedRole(long userId, String roleId) {
            userSelectedRoles.put(userId, roleId);
        }

        public static String getUserSelectedRole(long userId) {
            return userSelectedRoles.get(userId);
        }
    }
}
