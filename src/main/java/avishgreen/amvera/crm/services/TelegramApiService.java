package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.configs.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.reactions.SetMessageReaction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Сервис для взаимодействия с Telegram Bot API.
 * Инкапсулирует вызовы метода execute() и обработку TelegramApiException.
 */
@Service
@Slf4j // Аннотация Lombok для автоматического создания логгера
public class TelegramApiService {

    private final TelegramClient telegramClient;


    public TelegramApiService(AppConfig appConfig) {
        this.telegramClient = new OkHttpTelegramClient(appConfig.getTelegram().getToken());
    }


    /**
     * Отправляет ответное текстовое сообщение в чат.
     * @param chatId ID чата.
     * @param text Текст сообщения.
     * @param answerToMessageId ID сообщения на которое отправить ответ
     * @return Объект Message, представляющий отправленное сообщение, или null в случае ошибки.
     */
    public Message answerMessage(Long chatId, String text,  Integer answerToMessageId) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setReplyToMessageId(answerToMessageId);
        message.setParseMode(ParseMode.HTML); // Можно установить по умолчанию Markdown или HTML
        return executeSendMessage(message);
    }

    /**
     * Отправляет текстовое сообщение в чат.
     * @param chatId ID чата.
     * @param text Текст сообщения.
     * @return Объект Message, представляющий отправленное сообщение, или null в случае ошибки.
     */
    public Message sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setParseMode(ParseMode.HTML); // Можно установить по умолчанию Markdown или HTML
        return executeSendMessage(message);
    }

    /**
     * Отправляет текстовое сообщение с инлайн-клавиатурой.
     * @param chatId ID чата.
     * @param text Текст сообщения.
     * @param keyboard Инлайн-клавиатура.
     * @return Объект Message, представляющий отправленное сообщение, или null в случае ошибки.
     */
    public Message sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setReplyMarkup(keyboard);
        message.setParseMode(ParseMode.HTML);
        return executeSendMessage(message);
    }

    /**
     * Редактирует текст существующего сообщения.
     * @param chatId ID чата.
     * @param messageId ID сообщения, которое нужно отредактировать.
     * @param newText Новый текст сообщения.
     * @return True, если успешно, False в случае ошибки.
     */
    public boolean editMessageText(Long chatId, Integer messageId, String newText) {
        EditMessageText editMessageText = EditMessageText.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .text(newText)
                .parseMode(ParseMode.HTML)
                .build();
        return executeEditMessageText1(editMessageText);
    }

    /**
     * Редактирует только клавиатуру существующего сообщения.
     * @param chatId ID чата.
     * @param messageId ID сообщения, клавиатуру которого нужно отредактировать.
     * @param newKeyboard Новая инлайн-клавиатура.
     * @return True, если успешно, False в случае ошибки.
     */
    public boolean editMessageReplyMarkup(Long chatId, Integer messageId, InlineKeyboardMarkup newKeyboard) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(newKeyboard);
        return executeEditMessageReplyMarkup(editMessageReplyMarkup);
    }

    /**
     * Отвечает на CallbackQuery, чтобы убрать "часики" с кнопки.
     * @param callbackQueryId ID коллбэка.
     * @param text Опциональный текст уведомления (всплывающее уведомление или надпись на кнопке).
     * @return True, если успешно, False в случае ошибки.
     */
    public boolean answerCallbackQuery(String callbackQueryId, String text) {
        AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(text) // Если null, то уведомления не будет
        // .showAlert(true) // Можно сделать всплывающее окно
                .build();
        return executeAnswerCallbackQuery(answerCallbackQuery);
    }

    // --- Методы для низкоуровневого выполнения, инкапсулирующие TelegramApiException ---

    /**
     * Выполняет отправку SendMessage и обрабатывает исключения.
     * @param sendMessage Объект SendMessage для отправки.
     * @return Объект Message, если отправка успешна, иначе null.
     */
    public Message executeSendMessage(SendMessage sendMessage) {
        try {
            return telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}", sendMessage.getChatId(), e);
            // Можно добавить более сложную логику обработки ошибок, например, повторные попытки
            return null;
        }
    }

    /**
     * Выполняет редактирование EditMessageText и обрабатывает исключения.
     * @param editMessageText Объект EditMessageText.
     * @return True, если успешно, иначе False.
     */
    public boolean executeEditMessageText1(EditMessageText editMessageText) {
        try {
            telegramClient.execute(editMessageText); // execute() для EditMessageText возвращает Message или Boolean, в зависимости от версии API
            return true;
        } catch (TelegramApiException e) {
            log.error("Failed to edit message text in chat {} (message ID {}): {}",
                    editMessageText.getChatId(), editMessageText.getMessageId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Выполняет редактирование EditMessageReplyMarkup и обрабатывает исключения.
     * @param editMessageReplyMarkup Объект EditMessageReplyMarkup.
     * @return True, если успешно, иначе False.
     */
    public boolean executeEditMessageReplyMarkup(EditMessageReplyMarkup editMessageReplyMarkup) {
        try {
            telegramClient.execute(editMessageReplyMarkup); // execute() для EditMessageReplyMarkup возвращает Message или Boolean
            return true;
        } catch (TelegramApiException e) {
            log.error("Failed to edit message reply markup in chat {} (message ID {}): {}",
                    editMessageReplyMarkup.getChatId(), editMessageReplyMarkup.getMessageId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Выполняет ответ на AnswerCallbackQuery и обрабатывает исключения.
     * @param answerCallbackQuery Объект AnswerCallbackQuery.
     * @return True, если успешно, иначе False.
     */
    public boolean executeAnswerCallbackQuery(AnswerCallbackQuery answerCallbackQuery) {
        try {
            var res = telegramClient.execute(answerCallbackQuery);
            return true;
        } catch (TelegramApiException e) {
            log.warn("Failed to answer callback query {}: {}", answerCallbackQuery.getCallbackQueryId(), e.getMessage(), e);
            return false;
        }
    }

    public ChatMember executeGetChatMember(GetChatMember getChatMember) {
        try {
            return telegramClient.execute(getChatMember);
        } catch (TelegramApiException e) {
            log.error("Failed to get chat member {} in chat {} ",
                    getChatMember.getUserId(),getChatMember.getChatId(),  e);
            return null;
        }
    }

    public boolean executeMessageReaction(SetMessageReaction messageReaction) {
        try {
            return telegramClient.execute(messageReaction);
        } catch (TelegramApiException e) {
            log.error("Failed to set message {} reaction  in chat {} ",
                    messageReaction.getMessageId(),messageReaction.getChatId(),  e);
            return false;
        }
    }

    public boolean executePinMessage(PinChatMessage pinMessage) {
        try {
            return telegramClient.execute(pinMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to pin chat message {} in chat {} ",
                    pinMessage.getMessageId(),pinMessage.getChatId(),  e);
            return false;
        }
    }

    public Message executeForwardMessage(ForwardMessage forwardMessage) {
        try {
            return telegramClient.execute(forwardMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to forward message {} in chat {} ",
                    forwardMessage.getMessageId(),forwardMessage.getChatId(),  e);
            return null;
        }
    }

    public User executeGetMe(GetMe getMe) {
        try {
            return telegramClient.execute(getMe);
        } catch (TelegramApiException e) {
            log.error("Failed to get me", e);
            return null;
        }
    }

    public boolean editMessageTextAndReplyMarkup(Long chatId, Integer messageId, String newText, InlineKeyboardMarkup newKeyboard) {
        // Редактируем текст. Возвращает true, если успешно.
        boolean result;
        try {
            var editMessageText = EditMessageText.builder()
                    .chatId(String.valueOf(chatId))
                    .messageId(messageId)
                    .text(newText)
                    .replyMarkup(newKeyboard)
                    .parseMode(ParseMode.HTML)
                    .build();
            telegramClient.execute(editMessageText);
            result = true;
        } catch (TelegramApiException e){
            log.error("Failed to edit message text and reply markup", e);
            result = false;
        }
        return result;
    }

    public boolean deleteMessage(Long chatId, Integer messageId) {
        boolean result;
        try {
            var deleteMessage = DeleteMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .messageId(messageId)
                    .build();

            telegramClient.execute(deleteMessage);
            result = true;

        }catch (TelegramApiException e){
            log.error("Failed to edit message text and reply markup", e);
            result = false;
        }
        return result;
    }
}