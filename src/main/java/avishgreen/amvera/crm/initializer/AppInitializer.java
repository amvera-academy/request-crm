package avishgreen.amvera.crm.initializer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppInitializer implements CommandLineRunner {

    private final LiquibaseMigration liquibaseMigration;
    private final AppUsersInitializer appUsersInitializer;

    @Value("${spring.security.passwords.ivan}")
    private String ivanPassword;

    @Value("${spring.security.passwords.kirill}")
    private String kirillPassword;

    @Value("${app.update.passwords:false}")
    private boolean updatePasswords;

    @Override
    @SneakyThrows
    public void run(String... args) {
        // 1. Сначала запускаем миграцию Liquibase
        log.info("Запуск миграции Liquibase...");
        liquibaseMigration.start();
        log.info("Миграция Liquibase завершена.");

        // 2. Затем инициализируем пользователей, передав им нужные данные
        appUsersInitializer.initializeUsers(ivanPassword, kirillPassword, updatePasswords);
    }
}