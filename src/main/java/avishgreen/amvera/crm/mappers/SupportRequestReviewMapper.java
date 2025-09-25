package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.SupportRequestDto;
import avishgreen.amvera.crm.dto.SupportRequestReviewDto;
import avishgreen.amvera.crm.entities.SupportRequest;
import avishgreen.amvera.crm.entities.TelegramUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SupportRequestReviewMapper {

    @Mapping(target = "lastMessageId", source = "lastMessage.telegramMessageId")
    @Mapping(target = "information", source = "entity", qualifiedByName = "mapInformation")
    @Mapping(target = "lastMessageText", source = "lastMessage.messageText")
    SupportRequestReviewDto toDto(SupportRequest entity);

    @Named("mapInformation")
    default String mapInformation(SupportRequest entity) {
        String result = "";
        long senderId=0L;
        long authorId=0L;
        String senderName="";
        String authorName="";

        // Вспомогательная функция для получения имени (для чистоты)
        java.util.function.Function<TelegramUser, String> formatName = user ->
                String.format("%s %s",
                        user.getFirstName() != null ? user.getFirstName() : "",
                        user.getLastName() != null ? user.getLastName() : ""
                ).trim();

        if (entity.getLastMessage() != null && entity.getLastMessage().getSender() != null) {
            var sender = entity.getLastMessage().getSender();
            senderId = entity.getLastMessage().getSender().getId();
            senderName = formatName.apply(sender);
        }

        if(entity.getAuthor()!=null){
            var author = entity.getAuthor();
            authorId = author.getId();
            authorName = formatName.apply(author);
        }

        if(authorId==senderId){
            result = authorName;
        }else{
            result = senderName+" -> "+authorName;
        }

        return result;
    }

}