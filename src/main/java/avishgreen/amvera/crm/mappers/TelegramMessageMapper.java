package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.TelegramMessageDto;
import avishgreen.amvera.crm.entities.SupportRequest;
import avishgreen.amvera.crm.entities.TelegramMessage;
import avishgreen.amvera.crm.entities.TelegramUser;
import avishgreen.amvera.crm.services.UserNoteService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {TelegramUserMapper.class})
public interface TelegramMessageMapper {

    @Mapping(target = "supportRequestId", source = "supportRequest", qualifiedByName = "mapSupportRequestId")
//    @Mapping(target = "authorName",
//            expression = "java(message.getSender().getFirstName() " +
//                    " +\" @\"+ message.getSender().getUsername() + \" \"" +
//                    " + message.getSender().getLastName() )")
    @Mapping(target = "authorName", source = "sender", qualifiedByName = "mapSenderToName")
    @Mapping(target = "mediaFiles", source = "mediaFiles")
    TelegramMessageDto toDto(TelegramMessage message);

    @Mapping(target = "supportRequest", ignore = true)
    @Mapping(target = "mediaFiles", ignore = true)
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
}