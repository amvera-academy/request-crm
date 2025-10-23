package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.DisplayMediaDto;
import avishgreen.amvera.crm.entities.TelegramMedia;
import avishgreen.amvera.crm.enums.TelegramMediaUsageType;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DisplayMediaMapper {

    /**
     * Группирует плоский список записей TelegramMedia (PREVIEW и FULL_SIZE)
     * в список агрегированных DTO для отображения в UI.
     * @param mediaList Список всех TelegramMedia, связанных с сообщением.
     * @return Список DisplayMediaDto, где каждый DTO представляет собой один файл.
     */
    public List<DisplayMediaDto> mapToDisplayDto(List<TelegramMedia> mediaList) {
        if (mediaList == null || mediaList.isEmpty()) {
            return List.of();
        }

        // 1. Группируем записи по уникальному ID файла
        Map<String, List<TelegramMedia>> groupedMedia = mediaList.stream()
                .filter(media -> media.getMediaGroupUuid() != null) // Убедимся, что ID есть
                .collect(Collectors.groupingBy(TelegramMedia::getMediaGroupUuid));

        // 2. Преобразуем каждую группу в один DisplayMediaDto
        return groupedMedia.values().stream()
                .map(this::createDisplayMediaDto)
                .collect(Collectors.toList());
    }

    private DisplayMediaDto createDisplayMediaDto(List<TelegramMedia> mediaGroup) {
        Long previewId = null;
        Long fullsizeId = null;
        TelegramMedia previewMedia = null;
        TelegramMedia fullsizeMedia = null;

        // 1. Ищем PREVIEW и FULL_SIZE медиа
        for (TelegramMedia media : mediaGroup) {
            if (media.getUsageType() == TelegramMediaUsageType.PREVIEW) {
                previewId = media.getId();
                previewMedia = media;
            } else if (media.getUsageType() == TelegramMediaUsageType.FULL_SIZE) {
                fullsizeId = media.getId();
                fullsizeMedia = media;
            }
        }

        // 2. Определяем ID для ссылок (с резервом на PREVIEW)
        // Если FULL_SIZE не найден, используем PREVIEW ID как резервный для полного размера.
        Long effectiveFullsizeId = (fullsizeId != null) ? fullsizeId : previewId;

        // 3. Определяем источник метаданных. Приоритет: FULL_SIZE, затем PREVIEW.
        // Если source == null, это означает, что медиагруппа пуста или в ней нет нужных типов.
        TelegramMedia source = (fullsizeMedia != null) ? fullsizeMedia : previewMedia;

        if (source == null) {
            // Эта ситуация не должна произойти, если groupedMedia в mapToDisplayDto не пуста
            throw new IllegalStateException("Не удалось найти валидные метаданные для файла с unique ID: " + mediaGroup.get(0).getFileUniqueId());
        }

        // 4. Определяем флаг удаления (берем из того, что есть, приоритет у PREVIEW)
        boolean isDeleted = (previewMedia != null)
                ? previewMedia.getIsDeletedByTelegram()
                : source.getIsDeletedByTelegram(); // Если PREVIEW нет, берем из FULL_SIZE

        // 5. Обязательная проверка: нам нужен хотя бы один ID для работы
        if (previewId == null && fullsizeId == null) {
            // Если оба ID null, не можем отобразить медиафайл. Пропускаем.
            return null;
        }

        return DisplayMediaDto.builder()
                .previewMediaId(previewId) // Может быть null, если PREVIEW нет (тогда отобразится заглушка, но клик сработает)
                .fullsizeMediaId(effectiveFullsizeId) // Всегда будет не null, если хоть один ID был
                .fileUniqueId(source.getFileUniqueId())
                .mimeType(source.getMimeType())
                .fileSize(source.getFileSize())
                .width(source.getWidth())
                .height(source.getHeight())
                .deletedByTelegram(isDeleted)
                .build();
    }
}