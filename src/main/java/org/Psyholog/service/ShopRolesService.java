package org.Psyholog.service;

import org.Psyholog.entity.ShopRoles;
import org.Psyholog.repository.ShopRolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShopRolesService {
    private final ShopRolesRepository shopRolesRepository;

    @Autowired
    public ShopRolesService(ShopRolesRepository shopRolesRepository) {this.shopRolesRepository = shopRolesRepository;}

    // Добавление новой роли
    @Transactional
    public void addRole(Long id, int cost) {
        ShopRoles role = new ShopRoles();
        role.setId(id);
        role.setCost(cost);
        shopRolesRepository.save(role);
    }

    // Получить роль по ID
    @Transactional
    public Optional<ShopRoles> getRole(Long id) {
        return shopRolesRepository.findById(id);
    }

    // Удалить роль по ID
    @Transactional
    public void deleteRole(Long id) {
        shopRolesRepository.deleteById(id);
    }

    @Transactional
    public Integer getRolePriceById(Long roleId) {
        return shopRolesRepository.findById(roleId)
                .map(ShopRoles::getCost)
                .orElse(null);
    }

    @Transactional
    public Map<Long, Integer> getAllRolesWithPrices() {
        return shopRolesRepository.findAll().stream()
                .collect(Collectors.toMap(
                        ShopRoles::getId,
                        ShopRoles::getCost
                ));
    }

}
