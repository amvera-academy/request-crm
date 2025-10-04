package avishgreen.amvera.crm.services.telegramhandlers;

import avishgreen.amvera.crm.services.SupportRequestService;
import avishgreen.amvera.crm.utils.UserProcessingLocker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.locks.Lock;

/**
 * Базовый класс для обработчика
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramMessageHandler {

    private final SupportRequestService requestService;
    private final TelegramAntispamHandler antispamHandler;
    private final UserProcessingLocker locker;

    public void handleMessage(Update update)  {
        if (!update.hasMessage()) throw new RuntimeException("Update has no message!");
        var message = update.getMessage();
        var user = message.getFrom();


        //проверим, не спам ли это сообщение
        var messageId = message.getMessageId();
        var isSpam = antispamHandler.isSpam(messageId);
        if (isSpam) {
            log.warn("SPAM message id {} SKIPPED", messageId);
            return;
        }

        // Получаем блокировку, уникальную для этого пользователя
        // --- Используем lock() для принудительного ожидания (троттлинга) ---
        Lock userLock = locker.getLockForUser(user.getId());
        userLock.lock(); // !!! Здесь поток блокируется и ждет, если мьютекс занят !!!
        log.info("Handling message id {} from user {}", messageId, user.getId());
        try {
            requestService.processNewMessage(message, user);
        }finally {
            userLock.unlock();
        }

//        log.info("Received Update {}", message.getText());
    }

/*
    private void handleMyChatMember(Update update) {
        //отрабатывает когда бота добавляют куда то или меняют права
        var myChatMember =  update.getMyChatMember();
        var newChatMember =  update.getMyChatMember().getNewChatMember();


        //left, kicked, administrator, member(кроме добавления в канал,
        // там только админом)
        var status = newChatMember.getStatus();


        var botId = newChatMember.getUser().getId();
        var adminWhoAddBotToChat = myChatMember.getFrom();
        var adminId = adminWhoAddBotToChat.getId();
        // Безопасно получаем имя и фамилию, чтобы избежать NullPointerException
        var firstName = Optional.ofNullable(adminWhoAddBotToChat.getFirstName()).orElse("");
        var lastName = Optional.ofNullable(adminWhoAddBotToChat.getLastName()).orElse("");

        // Объединяем имя и фамилию
        var adminName = (firstName + " " + lastName).trim();

        // Безопасно получаем username
        var adminUsername = Optional.ofNullable(adminWhoAddBotToChat.getUserName()).orElse("unknown_username");

        var chat = myChatMember.getChat();
        var chatId = chat.getId();

        //channel, supergroup, group, private м.б. еще что то
        var chatType = chat.getType();

        log.info("Bot [{}] in [{}][{}] now has status [{}]", botId, chatType, chatId, status);

        // todo сделать сущность дата, идбота, тип чата, ид чата,
        // статус, ид имя и ссылку на админа
        // где логировать все эти изменения
        // из нее по последней записи определяем текущий статус бота в канале

        var statusChangeDTO =
                TelegramBotChatStatusChangeDTO.builder()
                .chatId(chatId)
                .chatType(TelegramChatType.fromString(chatType))
                .botId(botId)
                .adminId(adminId)
                .status(status)
                .adminUsername(adminUsername)
                .adminFullName(adminName)
                .timeStamp(Instant.now())
                .build();
        usersService.changeBotStatusInChat(statusChangeDTO);
    }
*/

}