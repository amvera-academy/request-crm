package avishgreen.amvera.crm.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "telegram_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramUser {

    @Id
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "last_activity", nullable = false)
    private Instant lastActivity;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

}