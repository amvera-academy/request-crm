package avishgreen.amvera.crm.entities;

import avishgreen.amvera.crm.enums.TelegramMediaUsageType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "telegram_media")
public class TelegramMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // fileId, который используется для вызова getFile. Может меняться со временем.
    @Column(name = "telegram_file_id", nullable = false)
    private String telegramFileId;

    // file_unique_id. Постоянный идентификатор файла в Telegram для дедупликации.
    @Column(name = "telegram_file_unique_id")
    private String fileUniqueId;

    @Column(name = "mime_type", length = 50)
    private String mimeType;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    // Флаг, указывающий, что файл был удален из хранилища Telegram и больше недоступен (404/410).
    // Используется для избежания повторных запросов к API.
    @Column(name = "is_deleted_by_telegram", nullable = false)
    @Builder.Default
    private Boolean isDeletedByTelegram = false;

    // Счетчик неудачных попыток загрузки файла
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    // Дата последней попытки для реализации задержки (back-off)
    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    // Ссылка на родительское сообщение
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private TelegramMessage message;

    @Column(name = "usage_type", length = 20)
    @Enumerated(EnumType.STRING)
    private TelegramMediaUsageType usageType; // Значения: "PREVIEW" или "FULL_SIZE"

    /**
     * Пользовательский ID для группировки PREVIEW и FULL_SIZE одной фотографии.
     * Генерируется при сохранении первого размера и используется для всех остальных.
     */
    @Column(name = "media_group_uuid", length = 36, nullable = false)
    private String mediaGroupUuid;
}
