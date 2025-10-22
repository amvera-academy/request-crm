package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.TelegramMessageDto;
import avishgreen.amvera.crm.entities.SupportRequest;
import avishgreen.amvera.crm.entities.TelegramMessage;
import avishgreen.amvera.crm.entities.TelegramUser;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {TelegramUserMapper.class, TelegramMediaMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        // ИСПОЛЬЗУЕМ: Если исходное поле равно null, MapStruct игнорирует маппинг.
        // НО для коллекций по умолчанию он должен ставить пустой список, если target не примитив.
        // Чтобы быть уверенным, что null-коллекция не передастся, используем:
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE // <-- Добавляем это
)public interface TelegramMessageMapper {
    @Mapping(target = "supportRequestId", source = "supportRequest", qualifiedByName = "mapSupportRequestId")
    @Mapping(target = "authorName", source = "sender", qualifiedByName = "mapSenderToName")
    @Mapping(target = "mediaFiles", source = "mediaFiles")
    @Mapping(target = "sender", source = "sender")
    TelegramMessageDto toDto(TelegramMessage message);

    // Добавляем маппинг для списка, если он нужен для SupportRequestDto
    List<TelegramMessageDto> toDtoList(List<TelegramMessage> messageList);

    @Mapping(target = "supportRequest", ignore = true)
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