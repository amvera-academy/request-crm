package avishgreen.amvera.crm.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private boolean prod;

    public boolean isProd() {
        return prod;
    }
}