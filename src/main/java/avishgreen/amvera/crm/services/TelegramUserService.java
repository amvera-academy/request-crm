package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.configs.AppConfig;
import avishgreen.amvera.crm.entities.TelegramUser;
import avishgreen.amvera.crm.repositories.TelegramUserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Setter
@RequiredArgsConstructor
public class TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;
    private final AppConfig appConfig;

    public TelegramUser getOrCreateBot(){
        var token = appConfig.getTelegram().getToken();
        // Переменная для хранения ID пользователя бота
        Long crmBotUserId;

        // Проверяем, что токен не пустой и содержит разделитель (двоеточие)
        if (token != null && token.contains(":")) {
            // Используем метод split(), чтобы разделить строку по первому двоеточию
            String[] parts = token.split(":", 2); // Ограничиваем разделение одним двоеточием

            // ID пользователя бота будет первой частью (индекс 0)
            crmBotUserId = Long.valueOf(parts[0]);
        }else {
            crmBotUserId = null;
        }

        if (crmBotUserId == null){
            throw new RuntimeException("CRM bot user id not found");
        }

        return telegramUserRepository.findById(crmBotUserId)
                .orElseGet(() -> {

                    // Создаем новую запись
                    TelegramUser botUser = new TelegramUser();

                    // Устанавливаем ID, чтобы JPA знал, что это наш специальный ID
                    botUser.setId(crmBotUserId);

                    // Устанавливаем минимально необходимые поля для бота
                    botUser.setUsername("CRM_System_Bot");
                    botUser.setFirstName("CRM Bot");
                    botUser.setLastActivity(Instant.now());
                    botUser.setLastUpdated(Instant.now());

                    // Сохраняем и возвращаем сохраненную сущность.
                    return telegramUserRepository.save(botUser);
                });

    }

    @Transactional
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