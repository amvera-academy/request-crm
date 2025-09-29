package avishgreen.amvera.crm.initializer;

import avishgreen.amvera.crm.configs.AppConfig;
import avishgreen.amvera.crm.entities.AppUser;
import avishgreen.amvera.crm.repositories.AppUserRepository;
import avishgreen.amvera.crm.services.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppUsersInitializer {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUserService appUserService;
    private final AppConfig appConfig;


    // Этот метод теперь принимает пароли извне
    public void initializeUsers() {
        var users = appConfig.getPasswords().getUsers();
        if (appConfig.getPasswords().getUpdatePasswords()==true) {
            log.info("Обновление паролей для всех пользователей...");
            updateUserPassword("ivan", users.get("ivan"));
            updateUserPassword("kirill", users.get("kirill"));
            updateUserPassword("latuk", users.get("latuk"));
        }
        createIfNotExist("ivan", users.get("ivan"));
        createIfNotExist("kirill", users.get("kirill"));
        createIfNotExist("latuk", users.get("latuk"));
    }

    private void createIfNotExist(String username, String password) {
        if (appUserRepository.findByUsername(username).isEmpty()) {
            AppUser user = AppUser.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(password))
                    .role("ROLE_USER")
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .isEnabled(true)
                    .build();
            appUserRepository.save(user);
            log.info("Пользователь '{}' создан.", username);
        }
    }

    private void updateUserPassword(String username, String newPassword) {
        appUserService.changePassword(username, newPassword);
    }
}