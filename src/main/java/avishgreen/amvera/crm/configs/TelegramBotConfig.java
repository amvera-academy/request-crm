package avishgreen.amvera.crm.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Класс конфигурации для Telegram-бота.
 * Загружает свойства из application.properties (или application.yml),
 * префикс 'telegrambot'.
 */
@Component // Помечаем как Spring-компонент
@ConfigurationProperties(prefix = "telegram") // Указываем префикс для свойств в файле конфигурации
@Data // Аннотация Lombok для автоматической генерации геттеров, сеттеров и т.д.
public class TelegramBotConfig {

    /**
     * Имя пользователя вашего бота (без '@').
     * Пример: MyAwesomeBot
     */
    private String username;

    /**
     * Токен API вашего бота, полученный от BotFather.
     * Пример: 123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11
     */
    private String token;
}