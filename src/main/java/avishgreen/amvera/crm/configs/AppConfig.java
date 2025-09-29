package avishgreen.amvera.crm.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "application")
@Getter
@Setter
public class AppConfig {
    private PasswordsProperties passwords = new PasswordsProperties();
    private AntispamProperties antispam = new AntispamProperties();
    private TelegramProperties telegram = new TelegramProperties();

    // Вложенный класс для секции 'passwords'
    @Getter
    @Setter
    public static class PasswordsProperties {
        private Map<String, String> users; // Или явное перечисление ivan, kirill, latuk
        private String updatePasswords;
    }

    // Вложенный класс для секции 'antispam'
    @Getter
    @Setter
    public static class AntispamProperties {
        private String url;
        private String bearerToken;
    }

    // Вложенный класс для секции 'telegram'
    @Getter
    @Setter
    public static class TelegramProperties {
        private String username;
        private String token;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}