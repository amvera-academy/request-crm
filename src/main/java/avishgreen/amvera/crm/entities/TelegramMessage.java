package avishgreen.amvera.crm.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "telegram_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramMessage {

    @Id
    @Column(name = "telegram_message_id", unique = true)
    private Integer telegramMessageId;

    @Column(name = "media_group_id") //для альбомов
    private String mediaGroupId;

    @Column(name = "message_text")
    private String messageText;

    @Column(name = "reply_to_message_id")
    private Integer replyToMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private TelegramUser sender;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_request_id")
    private SupportRequest supportRequest;

    // Дополнительные поля, которые могут быть полезны
    @Builder.Default
    @Column(name = "is_edited")
    private Boolean isEdited = false;

    @Builder.Default
    @Column(name = "is_media")
    private Boolean isMedia = false;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TelegramMedia> mediaFiles = new ArrayList<>();
}