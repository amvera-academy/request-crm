package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.TelegramMessageDto;
import avishgreen.amvera.crm.entities.SupportRequest;
import avishgreen.amvera.crm.entities.TelegramMedia;
import avishgreen.amvera.crm.entities.TelegramMessage;
import avishgreen.amvera.crm.entities.TelegramUser;
import avishgreen.amvera.crm.enums.TelegramMediaUsageType;
import avishgreen.amvera.crm.services.UserNoteService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {TelegramUserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TelegramMessageMapper {

    @Mapping(target = "supportRequestId", source = "supportRequest", qualifiedByName = "mapSupportRequestId")
//    @Mapping(target = "authorName",
//            expression = "java(message.getSender().getFirstName() " +
//                    " +\" @\"+ message.getSender().getUsername() + \" \"" +
//                    " + message.getSender().getLastName() )")
    @Mapping(target = "authorName", source = "sender", qualifiedByName = "mapSenderToName")
    @Mapping(target = "previewMediaId", source = "mediaFiles", qualifiedByName = "getPreviewId")
    @Mapping(target = "fullSizeMediaId", source = "mediaFiles", qualifiedByName = "getFullSizeId")
    TelegramMessageDto toDto(TelegramMessage message);

    @Mapping(target = "supportRequest", ignore = true)
//    @Mapping(target = "previewMediaId", ignore = true)
//    @Mapping(target = "fullSizeMediaId", ignore = true)
    TelegramMessage toEntity(TelegramMessageDto dto);

    @Named("mapSupportRequestId")
    default Long mapSupportRequestId(SupportRequest supportRequest) {
        return supportRequest != null ? supportRequest.getId() : null;
    }

    @Named("mapSenderToName")
    default String mapSenderToName(TelegramUser sender) {
        if (sender == null) {
            return "Неизвестный"; // Заглушка, если отправитель отсутствует
        }

        String firstName = sender.getFirstName() != null ? sender.getFirstName() : "";
        String username = sender.getUsername() != null ? "@" + sender.getUsername() : "";
        String lastName = sender.getLastName() != null ? sender.getLastName() : "";
        String userId = sender.getId() != null ? "(" + sender.getId()+")" : "";

        // Формируем имя, убирая лишние пробелы, если поля пустые
        String namePart = String.join(" ", firstName, lastName).trim();

        if (namePart.isEmpty() && username.isEmpty()) {
            return "ID " + sender.getId(); // Если нет имени и юзернейма
        }

        // Возвращаем: Имя Фамилия @username
        if (!namePart.isEmpty() && !username.isEmpty()) {
            return String.join(" ",namePart,username,userId).trim();
        } else if (!namePart.isEmpty()) {
            return String.join(" ",namePart,userId).trim();
        } else {
            return String.join(" ",username,userId).trim();
        }
    }

    /**
     * Ищет медиафайл с usageType == PREVIEW и возвращает его ID.
     */
    @Named("getPreviewId")
    default Long getPreviewId(List<TelegramMedia> mediaFiles) {
        if (mediaFiles == null) {
            return null;
        }

        return mediaFiles.stream()
                .filter(media -> media.getUsageType() == TelegramMediaUsageType.PREVIEW)
                .map(TelegramMedia::getId) // Извлекаем ID сущности
                .findFirst()
                .orElse(null);
    }

    /**
     * Ищет медиафайл с usageType == FULL_SIZE и возвращает его ID.
     */
    @Named("getFullSizeId")
    default Long getFullSizeId(List<TelegramMedia> mediaFiles) {
        if (mediaFiles == null) {
            return null;
        }

        return mediaFiles.stream()
                .filter(media -> media.getUsageType() == TelegramMediaUsageType.FULL_SIZE)
                .map(TelegramMedia::getId) // Извлекаем ID сущности
                .findFirst()
                .orElse(null);
    }
}