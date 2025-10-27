package avishgreen.amvera.crm.factories;

import avishgreen.amvera.crm.configs.AppConfig;
import avishgreen.amvera.crm.entities.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramClientFactory {

    // Кеш для переиспользования клиентов, чтобы не создавать их при каждом запросе
    private final ConcurrentMap<String, TelegramClient> clientCache = new ConcurrentHashMap<>();
    private final AppConfig appConfig;

    /**
     * Возвращает или создает TelegramClient для указанного токена.
     * @param botToken Токен бота.
     * @return TelegramClient.
     */
    public TelegramClient getTelegramClient() {
        //Получим из контекста botToken
        String appUserBotToken;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AppUser appUser) {
            appUserBotToken = appUser.getBotToken(); // <-- Получаем ID напрямую из сущности
        }else{
            // В случае анонимного доступа, токена или другой проблемы аутентификации
            throw new IllegalStateException("AppUser not found in security context: principal is not AppUser.");
        }

        TelegramClient telegramClient;
        if(appUserBotToken!=null&&!appUserBotToken.equals("")){
            // computeIfAbsent гарантирует, что клиент будет создан только один раз для каждого токена
            telegramClient = clientCache.computeIfAbsent(appUserBotToken, OkHttpTelegramClient::new);
        }else{
            // Если у пользователя токен не указан, возвращаем дефолт клиента
            var defaultBotToken = appConfig.getTelegram().getToken();
            log.warn("AppUser {} has no botToken in settings. Will use default!",appUser.getUsername());
            telegramClient = clientCache.computeIfAbsent(defaultBotToken, OkHttpTelegramClient::new);
        }
        return telegramClient;
    }
}