package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.configs.AppConfig;
import avishgreen.amvera.crm.exceptions.TelegramFileNotFoundException;
import avishgreen.amvera.crm.factories.TelegramClientFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.*;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.reactions.SetMessageReaction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Сервис для взаимодействия с Telegram Bot API.
 * Инкапсулирует вызовы метода execute() и обработку TelegramApiException.
 */
@Service
@Slf4j // Аннотация Lombok для автоматического создания логгера
public class TelegramApiService {

    private final OkHttpClient httpClientForDownload = new OkHttpClient(); // Отдельный клиент для скачивания
    private final AppUserService appUserService;
    private final TelegramClientFactory telegramClientFactory;
    private final AppConfig appConfig;

    public TelegramApiService(AppUserService appUserService, TelegramClientFactory telegramClientFactory, AppConfig appConfig) {
        this.appUserService = appUserService;
        this.telegramClientFactory = telegramClientFactory;
        this.appConfig = appConfig;
    }

    // --- Методы для низкоуровневого выполнения, инкапсулирующие TelegramApiException ---

    /**
     * Выполняет отправку SendMessage и обрабатывает исключения.
     * @param sendMessage Объект SendMessage для отправки.
     * @return Объект Message, если отправка успешна, иначе null.
     */
    public Message executeSendMessage(SendMessage sendMessage) {
        sendMessage.setParseMode(ParseMode.HTML); // Можно установить по умолчанию Markdown или HTML

        var telegramClient = telegramClientFactory.getTelegramClient();

        try {
            return telegramClient.execute(sendMessage);
        } catch (TelegramApiRequestException e) {
            // Проверяем, является ли ошибка "Bad Request" (код 400)
            if (e.getErrorCode() == 400) {

                // Проверяем не удалено ли сообщение
                final String messageNotFound = "message to be replied not found";

                if (e.getApiResponse().contains(messageNotFound)) {

                    // Это специфическая ошибка "сообщение удалено".
                    log.warn("Failed to send reply to chat {}. Reason: Message to be replied was deleted.",
                            sendMessage.getChatId());

                    // Создаем и выбрасываем более информативное исключение
                    // Мы используем IllegalArgumentException, чтобы указать на проблему с входными данными.
                    throw new IllegalArgumentException(
                            "Невозможно отправить ответ. Исходное сообщение пользователя было удалено.", e);
                }
            }

            // Если это другая ошибка API (например, "Chat not found"), перебрасываем ее или логируем
            log.error("Failed to send message to chat {} due to Telegram API request error.",
                    sendMessage.getChatId(), e);
            return null;
            //throw e; // Перебрасываем API-ошибку для обработки на более высоком уровне
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
        var telegramClient = telegramClientFactory.getTelegramClient();
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
        var telegramClient = telegramClientFactory.getTelegramClient();
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
        var telegramClient = telegramClientFactory.getTelegramClient();
        try {
            var res = telegramClient.execute(answerCallbackQuery);
            return true;
        } catch (TelegramApiException e) {
            log.warn("Failed to answer callback query {}: {}", answerCallbackQuery.getCallbackQueryId(), e.getMessage(), e);
            return false;
        }
    }

    public ChatMember executeGetChatMember(GetChatMember getChatMember) {
        var telegramClient = telegramClientFactory.getTelegramClient();
        try {
            return telegramClient.execute(getChatMember);
        } catch (TelegramApiException e) {
            log.error("Failed to get chat member {} in chat {} ",
                    getChatMember.getUserId(),getChatMember.getChatId(),  e);
            return null;
        }
    }

    public boolean executeMessageReaction(SetMessageReaction messageReaction) {
        var telegramClient = telegramClientFactory.getTelegramClient();
        try {
            return telegramClient.execute(messageReaction);
        } catch (TelegramApiException e) {
            log.error("Failed to set message {} reaction  in chat {} ",
                    messageReaction.getMessageId(),messageReaction.getChatId(),  e);
            return false;
        }
    }

    public boolean executePinMessage(PinChatMessage pinMessage) {
        var telegramClient = telegramClientFactory.getTelegramClient();
        try {
            return telegramClient.execute(pinMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to pin chat message {} in chat {} ",
                    pinMessage.getMessageId(),pinMessage.getChatId(),  e);
            return false;
        }
    }

    public Message executeForwardMessage(ForwardMessage forwardMessage) {
        var telegramClient = telegramClientFactory.getTelegramClient();
        try {
            return telegramClient.execute(forwardMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to forward message {} in chat {} ",
                    forwardMessage.getMessageId(),forwardMessage.getChatId(),  e);
            return null;
        }
    }

    public User executeGetMe(GetMe getMe) {
        var telegramClient = telegramClientFactory.getTelegramClient();
        try {
            return telegramClient.execute(getMe);
        } catch (TelegramApiException e) {
            log.error("Failed to get me", e);
            return null;
        }
    }

    public boolean editMessageTextAndReplyMarkup(Long chatId, Integer messageId, String newText, InlineKeyboardMarkup newKeyboard) {
        var telegramClient = telegramClientFactory.getTelegramClient();
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
        var telegramClient = telegramClientFactory.getTelegramClient();
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

    /**
     * Получает информацию о файле по его ID, включая file_path, необходимый для скачивания.
     * @param fileId Идентификатор файла.
     * @return Объект File, содержащий метаданные, или null в случае ошибки.
     */
    public File getFile(String fileId) {
        var telegramClient = telegramClientFactory.getTelegramClient();
        GetFile getFileMethod = GetFile.builder().fileId(fileId).build();
        try {
            return telegramClient.execute(getFileMethod);
        } catch (TelegramApiException e) {
            log.error("Failed to get file information for file ID {}", fileId, e);
            return null;
        }
    }

    /**
     * Скачивает файл по его относительному пути (file_path).
     * Построение URL: https://api.telegram.org/file/bot<token>/<file_path>
     * @param filePath Относительный путь к файлу, полученный из объекта File.
     * @return InputStream скачанного файла. Вызывающая сторона должна закрыть этот поток.
     * @throws IOException Если произошла ошибка сети или ответа.
     */
    public InputStream downloadFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }

        var botToken = appConfig.getTelegram().getToken(); //скачиваем под дефолтным ботом
        String downloadUrl = String.format("https://api.telegram.org/file/bot%s/%s", botToken, filePath);

        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();

        try {
            Response response = httpClientForDownload.newCall(request).execute();

            if (response.isSuccessful()) {
                // Успешное скачивание
                return response.body().byteStream();
            } else {
                int code = response.code();
                String responseBody = response.body() != null ? response.body().string() : "No body";
                response.close();

                // 404 Not Found - наиболее вероятный код, если файл удален или путь неверен
                if (code == 404) {
                    log.warn("File not found on Telegram server for path: {}", filePath);
                    throw new TelegramFileNotFoundException("Файл удален или не найден на сервере Telegram (HTTP 404).");
                }
                // 400 Bad Request, 500 Server Error и т.д.
                else {
                    log.error("Failed to download file from {} with HTTP code {}. Response body: {}",
                            downloadUrl, code, responseBody);
                    throw new IOException("Ошибка HTTP при скачивании файла. Код: " + code);
                }
            }
        } catch (IOException e) {
            // Здесь перехватываются сетевые ошибки OkHttp (например, таймаут, недоступность хоста)
            if (!(e instanceof TelegramFileNotFoundException)) {
                log.error("Network error while trying to download file from {}", downloadUrl, e);
            }
            throw e; // Перебрасываем для обработки на следующем уровне
        }
    }
}