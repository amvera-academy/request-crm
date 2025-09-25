package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.entities.TelegramUser;
import avishgreen.amvera.crm.repositories.TelegramUserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;

    public TelegramUser getOrCreateIfNeed(@NonNull User user) {
        var userId = user.getId();
        var telegramUser = telegramUserRepository.findById(userId)
                .orElseGet(() -> updateUserData(user));

        if (telegramUser.getLastActivity().isBefore(Instant.now().minus(1, ChronoUnit.DAYS))) {
            updateUserData(user);
        }
        return  telegramUser;
    }

    @Transactional
    protected TelegramUser updateUserData(@NonNull User user) {
        var telegramUser = TelegramUser.builder()
                .id(user.getId())
                .username(user.getUserName())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUserName())
                .lastActivity(Instant.now())
                .lastUpdated(Instant.now())
                .build();
        telegramUserRepository.save(telegramUser);
        return telegramUser;
    }

    public TelegramUser findById(@NonNull Long id) {
        return telegramUserRepository.findById(id)
                .orElseThrow(() ->new IllegalArgumentException("Cant find user with id "+id));
    }
}