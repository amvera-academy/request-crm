package avishgreen.amvera.crm;

import avishgreen.amvera.crm.initializer.LiquibaseMigration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@Slf4j
public class MainApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(MainApplication.class, args);

        //миграции теперь вызываются в AppInitializer

        log.info("HELLO. Application Started");
    }
}
