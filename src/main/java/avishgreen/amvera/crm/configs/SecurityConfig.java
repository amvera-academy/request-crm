package avishgreen.amvera.crm.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {
    @Value("${spring.security.passwords.ivan}")
    private String ivanPassword;

    @Value("${spring.security.passwords.kirill}")
    private String kirillPassword;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                    .anyRequest().authenticated()
//                    .anyRequest().permitAll() // Разрешить доступ всем к любым запросам
            )
            .formLogin(withDefaults())
            // Добавляем конфигурацию для logout
            .logout(LogoutConfigurer::permitAll);
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails ivanCRMUser = User.withUsername("ivan")
                .password(passwordEncoder().encode(ivanPassword))
                .roles("USER")
                .build();

        UserDetails kirillCRMUser = User.withUsername("kirill")
                .password(passwordEncoder().encode(kirillPassword))
                .roles("user")
                .build();

        return new InMemoryUserDetailsManager(ivanCRMUser, kirillCRMUser);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}