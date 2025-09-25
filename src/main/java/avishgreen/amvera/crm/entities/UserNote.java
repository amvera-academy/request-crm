package avishgreen.amvera.crm.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note_text", nullable = false)
    private String noteText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private TelegramUser author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private AppUser creator;
}