package org.Psyholog.service;

import org.Psyholog.entity.UserCoins;
import org.Psyholog.repository.UserCoinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserCoinService {

    private final UserCoinRepository userCoinRepository;

    @Autowired
    public UserCoinService(UserCoinRepository userCoinRepository) {
        this.userCoinRepository = userCoinRepository;
    }

    public UserCoins getOrCreateUser(Long userId) {
        return userCoinRepository.findById(userId).orElseGet(() -> {
            UserCoins newUser = new UserCoins();
            newUser.setUserId(userId);
            newUser.setCoins(0);
            return userCoinRepository.save(newUser);
        });
    }

    @Transactional
    public void addCoins(Long userId, int amount) {
        UserCoins user = getOrCreateUser(userId);
        user.setCoins(user.getCoins() + amount);
        userCoinRepository.save(user);
    }

    @Transactional
    public boolean removeCoins(Long userId, int amount) {
        UserCoins user = getOrCreateUser(userId);

        if (user.getCoins() < amount) {
            return false;
        }

        userCoinRepository.removeCoinsByUserId(userId, amount);
        return true;
    }

    public int getBalance(Long userId) {
        return getOrCreateUser(userId).getCoins();
    }
}
