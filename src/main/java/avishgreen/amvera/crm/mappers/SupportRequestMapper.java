package avishgreen.amvera.crm.mappers;

import avishgreen.amvera.crm.dto.SupportRequestDto;
import avishgreen.amvera.crm.entities.SupportRequest;
import avishgreen.amvera.crm.entities.TelegramUser;
import avishgreen.amvera.crm.services.UserNoteService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {TelegramMessageMapper.class})
public abstract class SupportRequestMapper {

    @Autowired
    protected UserNoteService userNoteService;

    @Mapping(target = "lastMessageId", source = "lastMessage.telegramMessageId")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName",
            expression = """
                java("%s %s".formatted(
                    entity.getAuthor().getFirstName() != null ? entity.getAuthor().getFirstName() : "",
                    entity.getAuthor().getLastName() != null ? entity.getAuthor().getLastName() : ""))
                """)
    @Mapping(target = "participantNames", source = ".", qualifiedByName = "toParticipantNames")
    @Mapping(target = "lastMessageText", source = "lastMessage.messageText")
    @Mapping(target = "note", source = ".", qualifiedByName = "toNote")
    public abstract SupportRequestDto toDto(SupportRequest entity);

    @Named("toParticipantNames")
    protected List<String> toParticipantNames(SupportRequest supportRequest) {
        if (supportRequest.getParticipants() == null || supportRequest.getAuthor() == null) {
            return null;
        }

        Long authorId = supportRequest.getAuthor().getId();

        return supportRequest.getParticipants().stream()
                .filter(participant -> !participant.getId().equals(authorId))
                .map(participant -> String.format("%s %s",
                        participant.getFirstName() != null ? participant.getFirstName() : "",
                        participant.getLastName() != null ? participant.getLastName() : ""
                ))
                .collect(Collectors.toList());
    }

    @Named("toNote")
    protected String toNote(SupportRequest supportRequest) {
        if (supportRequest == null) {
            return null;
        }
        List<String> notes = userNoteService.getNotesByAuthorId(supportRequest.getAuthor().getId())
                .stream()
                .map(noteDto -> noteDto.noteText())
                .collect(Collectors.toList());

        return String.join("\n", notes);
    }
}