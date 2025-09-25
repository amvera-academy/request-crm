package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.dto.UserNoteDto;
import avishgreen.amvera.crm.entities.AppUser;
import avishgreen.amvera.crm.entities.TelegramUser;
import avishgreen.amvera.crm.entities.UserNote;
import avishgreen.amvera.crm.mappers.UserNoteMapper;
import avishgreen.amvera.crm.repositories.UserNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserNoteService {
    private final UserNoteRepository userNoteRepository;
    private final UserNoteMapper userNoteMapper;
    private  final TelegramUserService telegramUserService;
    private final AppUserService appUserService;

    public List<UserNoteDto> getNotesByAuthorId(Long authorId) {
        return userNoteRepository.findByAuthorId(authorId)
                .stream()
                .map(userNoteMapper::toDto)
                .collect(Collectors.toList());
    }

    public void saveNote(UserNoteDto noteDto) {
        // Находим сущности автора и создателя по их ID
        TelegramUser author = telegramUserService.findById(noteDto.authorId());
        AppUser creator = appUserService.findById(noteDto.creatorId());

        // Создаем новую сущность UserNote
        UserNote userNote = new UserNote();
        userNote.setNoteText(noteDto.noteText());
        userNote.setAuthor(author);
        userNote.setCreator(creator);

        // Сохраняем сущность
        userNoteRepository.save(userNote);
    }
}