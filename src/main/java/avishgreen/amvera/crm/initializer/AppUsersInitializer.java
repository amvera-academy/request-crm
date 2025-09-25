package avishgreen.amvera.crm.initializer;

import avishgreen.amvera.crm.entities.AppUser;
import avishgreen.amvera.crm.repositories.AppUserRepository;
import avishgreen.amvera.crm.services.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppUsersInitializer {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUserService appUserService;

    // Этот метод теперь принимает пароли извне
    public void initializeUsers(String ivanPassword, String kirillPassword, boolean updatePasswords) {
        if (updatePasswords) {
            log.info("Обновление паролей для всех пользователей...");
            updateUserPassword("ivan", ivanPassword);
            updateUserPassword("kirill", kirillPassword);
        }
        createIfNotExist("ivan", ivanPassword);
        createIfNotExist("kirill", kirillPassword);
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