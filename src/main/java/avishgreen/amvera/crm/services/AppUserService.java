package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.configs.AppConfig;
import avishgreen.amvera.crm.entities.AppUser;
import avishgreen.amvera.crm.factories.TelegramClientFactory;
import avishgreen.amvera.crm.repositories.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final TelegramClientFactory telegramClientFactory;
    private final AppConfig appConfig;

    @Override
    public AppUser loadUserByUsername(String username) throws UsernameNotFoundException {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    // Новый метод для обновления пароля
    public void changePassword(String username, String newPassword) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Хэшируем новый пароль перед сохранением
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        appUserRepository.save(user);
    }

    /**
     * Обновляет данные пользователя (никнейм, роль, статус активности).
     * @param id ID пользователя.
     * @param updatedUser Обновленные данные пользователя из формы.
     */
    public void updateUser(Long id, AppUser updatedUser) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));

        // Обновление основных полей
        user.setUsername(updatedUser.getUsername());
        user.setRole(updatedUser.getRole());
        user.setEnabled(updatedUser.isEnabled());
        user.setBotToken(updatedUser.getBotToken());

        appUserRepository.save(user);
    }

    public AppUser findById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));
    }

    /**
     * Возвращает список всех пользователей из базы данных.
     */
    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }


}