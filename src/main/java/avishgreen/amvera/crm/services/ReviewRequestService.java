package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.dto.SupportMessageDto;
import avishgreen.amvera.crm.dto.SupportRequestDto;
import avishgreen.amvera.crm.enums.SupportRequestStatus;
import avishgreen.amvera.crm.models.SupportRequestModel;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Getter
public class ReviewRequestService {

    private List<SupportRequestModel> supportRequestModels;

    private final Random random = new Random();
    private final List<String> notes = Arrays.asList(
            "Проблема решена",
            "Ожидается ответ",
            "В работе",
            "Требуется уточнение"
    );

    private final List<String> sampleSentences = Arrays.asList(
            "Здравствуйте, у меня возникла проблема с доступом к личному кабинету.",
            "При попытке входа система выдает ошибку с кодом 404.",
            "Я уже пробовал сбросить пароль, но это не помогло.",
            "Пожалуйста, помогите разобраться, в чем может быть причина.",
            "Добрый день. Я не могу загрузить фотографии для своего объявления.",
            "Каждый раз, когда я нажимаю 'Загрузить', страница просто обновляется без результата.",
            "Эта проблема появилась сегодня утром, до этого все работало отлично.",
            "Можете ли вы проверить, нет ли технических сбоев на вашей стороне?",
            "Здравствуйте. Я не получил уведомление о новом сообщении в чате.",
            "Проверял настройки, push-уведомления включены, но сообщения не приходят.",
            "Такое ощущение, что уведомления просто не доходят до моего устройства."
    );

    @PostConstruct
    public void init() {
        this.supportRequestModels = generateSupportRequests();
    }

    public List<SupportRequestModel> generateSupportRequests() {
        List<SupportRequestModel> supportRequestModelList = new ArrayList<>();
        List<SupportRequestStatus> statuses = Arrays.asList(SupportRequestStatus.values());

        for (long i = 1; i <= 15; i++) {
            String randomNote = notes.get(random.nextInt(notes.size()));
            SupportRequestStatus randomStatus = statuses.get(random.nextInt(statuses.size()));

            List<SupportMessageDto> messages = generateRandomMessages(i);
            LocalDateTime lastUpdateTime = findLastMessageTime(messages);

            // Заполняем автора на основе самого первого сообщения в сгенерированном списке
            String author = findInitialAuthor(messages);

            List<String> participants = messages.stream()
                    .map(SupportMessageDto::author)
                    .distinct()
                    .filter(participant -> !participant.equals(author))
                    .collect(Collectors.toList());

            supportRequestModelList.add(new SupportRequestModel(
                    i,
                    messages,
                    author,
                    participants,
                    lastUpdateTime,
                    randomNote,
                    randomStatus
            ));
        }
        return supportRequestModelList;
    }

    // Добавляем метод для поиска автора самого раннего сообщения
    private String findInitialAuthor(List<SupportMessageDto> messages) {
        return messages.stream()
                .min(Comparator.comparing(SupportMessageDto::timestamp))
                .map(SupportMessageDto::author)
                .orElse("unknown");
    }

    private LocalDateTime findLastMessageTime(List<SupportMessageDto> messages) {
        return messages.stream()
                .map(SupportMessageDto::timestamp)
                .max(Comparator.naturalOrder())
                .orElse(LocalDateTime.now());
    }

    private List<SupportMessageDto> generateRandomMessages(long requestId) {
        List<SupportMessageDto> messages = new ArrayList<>();
        int messageCount = random.nextInt(3) + 2;
        String[] possibleAuthors = {"user1", "user2", "user3", "admin", "moderator"};

        for (int i = 0; i < messageCount; i++) {
            String text = generateLongMessage();
            LocalDateTime timestamp = LocalDateTime.now().minusHours(random.nextInt(48));
            String author = possibleAuthors[random.nextInt(possibleAuthors.length)];
            messages.add(new SupportMessageDto(text, timestamp, author));
        }
        return messages;
    }

    private String generateLongMessage() {
        StringBuilder sb = new StringBuilder();
        int sentenceCount = random.nextInt(3) + 2;
        for (int j = 0; j < sentenceCount; j++) {
            sb.append(sampleSentences.get(random.nextInt(sampleSentences.size())));
            if (j < sentenceCount - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public void updateRequestStatus(Long id, SupportRequestStatus status) {
        for (SupportRequestModel request : supportRequestModels) {
            if (request.getId().equals(id)) {
                request.setStatus(status);
                break;
            }
        }
    }

    public SupportRequestDto getSupportRequestModelById(Long id) {
        return supportRequestModels.stream()
                .filter(request -> request.getId().equals(id))
                .findFirst()
                .map(this::mapToSupportRequestDto)
                .orElse(null);
    }

    private SupportRequestDto mapToSupportRequestDto(SupportRequestModel model) {
        // Сортируем сообщения перед тем, как положить их в DTO
        List<SupportMessageDto> messageDtos = new ArrayList<>(model.getMessages());
        messageDtos.sort(Comparator.comparing(SupportMessageDto::timestamp).reversed());

        // Получаем автора на основе самого раннего сообщения
        String initialAuthor = findInitialAuthor(model.getMessages());

        return new SupportRequestDto(
                model.getId(),
                messageDtos,
                initialAuthor,
                model.getParticipants(),
                model.getLastUpdateTime(),
                model.getNote(),
                model.getStatus()
        );
    }

    public List<SupportRequestDto> getReviewRequestsForView() {
        return supportRequestModels.stream()
                .map(model -> {
                    List<SupportMessageDto> messageDtos = new ArrayList<>(model.getMessages());

                    // Сортируем сообщения по дате-времени.
                    // Добавляем проверку на null, чтобы избежать ошибок,
                    // если в данных окажется некорректная запись.
                    messageDtos.sort(Comparator.comparing(
                            SupportMessageDto::timestamp,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    ));

                    // Получаем автора на основе самого раннего сообщения
                    String initialAuthor = findInitialAuthor(model.getMessages());

                    return new SupportRequestDto(
                            model.getId(),
                            messageDtos,
                            initialAuthor,
                            model.getParticipants(),
                            model.getLastUpdateTime(),
                            model.getNote(),
                            model.getStatus()
                    );
                })
                // Добавляем сортировку запросов по lastUpdateTime (от новых к старым)
                .sorted(Comparator.comparing(
                        SupportRequestDto::lastUpdateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))

                .collect(Collectors.toList());
    }}
