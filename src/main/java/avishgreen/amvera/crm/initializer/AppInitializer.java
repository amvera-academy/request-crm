package avishgreen.amvera.crm.initializer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppInitializer implements CommandLineRunner {

    private final LiquibaseMigration liquibaseMigration;
    private final AppUsersInitializer appUsersInitializer;

    @Override
    @SneakyThrows
    public void run(String... args) {
        // запускаем миграцию Liquibase
        log.info("Запуск миграции Liquibase...");
        liquibaseMigration.start();
        log.info("Миграция Liquibase завершена.");

        // инициализируем пользователей, передав им нужные данные
        appUsersInitializer.initializeUsers();
    }
}