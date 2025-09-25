package avishgreen.amvera.crm.entities;

import avishgreen.amvera.crm.enums.SupportRequestStatusType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "support_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @OneToOne
    @JoinColumn(name = "last_message_id")
    private TelegramMessage lastMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private TelegramUser author;

    @JoinColumn(name = "chat_id", nullable = false)
    private Long chatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SupportRequestStatusType status;

    @OneToMany(mappedBy = "supportRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("sentAt DESC")
    private List<TelegramMessage> messages = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "support_request_participants",
            joinColumns = @JoinColumn(name = "support_request_id"),
            inverseJoinColumns = @JoinColumn(name = "participant_id")
    )
    @Builder.Default
    private Set<TelegramUser> participants = new HashSet<>();
}