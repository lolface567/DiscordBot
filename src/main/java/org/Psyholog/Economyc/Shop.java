package org.Psyholog.Economyc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Shop extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Stats.class);
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("shop")) {
            Member member = event.getMember();
            Guild guild = event.getGuild();
            Map<Long, Integer> roles = DatabaseManager.getShopRoles(); // Получаем список ролей и их цен

            if (roles.isEmpty()) {
                event.reply("В магазине пока нет товаров.").setEphemeral(true).queue();
                return;
            }

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("🛒 Магазин сервера")
                    .setColor(Color.CYAN)
                    .addField("\uD83D\uDCB0Ваш баланс: ", String.valueOf(DatabaseManager.getBalance(member.getId())), false)
                    .setDescription("Выберите роль из списка ниже, а затем нажмите 'Купить'.");

            // Создаем выпадающее меню с ролями
            StringSelectMenu.Builder menu = StringSelectMenu.create("shop:select")
                    .setPlaceholder("Выберите роль для покупки");

            for (Map.Entry<Long, Integer> entry : roles.entrySet()) {
                assert guild != null;
                Role role = guild.getRoleById(entry.getKey());
                if (role != null) {
                    menu.addOption(role.getName() + " - " + entry.getValue() + " монет", String.valueOf(role.getId()));
                }
            }

            // Кнопка покупки
            net.dv8tion.jda.api.interactions.components.buttons.Button buyButton =
                    net.dv8tion.jda.api.interactions.components.buttons.Button.success("shop:buy", "Купить");

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
            DatabaseManager.addRoleToShop(role_id, role_cost);
            event.reply("Роль: добавлена").setEphemeral(true).queue();
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

            int roleCost = DatabaseManager.getRoleCost(Long.parseLong(selectedRoleId));
            int userBalance = DatabaseManager.getBalance(String.valueOf(userId));

            if (userBalance < roleCost) {
                event.reply("Недостаточно средств! Ваш баланс: " + userBalance + " монет.").setEphemeral(true).queue();
                return;
            }

            if(member.getRoles().contains(role)){
                event.reply("У вас уже есть роль: " + role.getName()).setEphemeral(true).queue();
                return;
            }

            // Списываем монеты и выдаем роль
            DatabaseManager.removeCoins(userId, roleCost);
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
