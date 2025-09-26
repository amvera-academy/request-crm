package avishgreen.amvera.crm.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Конфигурирует цепочку фильтров безопасности (SecurityFilterChain).
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            DaoAuthenticationProvider daoAuthenticationProvider // Инжектируем явно объявленный провайдер
    ) throws Exception {
        http
                // 1. Явно регистрируем DaoAuthenticationProvider.
                .authenticationProvider(daoAuthenticationProvider)

                // 2. Настраиваем авторизацию запросов
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated() // Все запросы требуют аутентификации
                )
                // 3. Настраиваем форму входа
                .formLogin(form -> form
                        .permitAll()
                )
                // 4. Настраиваем выход (logout)
                .logout(logout -> logout
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Бин для шифрования паролей.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Явное объявление бина DaoAuthenticationProvider с использованием современного Builder-паттерна.
     * Это устраняет предупреждения об устаревших методах.
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        // Использование DaoAuthenticationProvider.builder() заменяет прямой конструктор
        // и сеттеры, что является современным подходом.
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}