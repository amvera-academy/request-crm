package avishgreen.amvera.crm.enums;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Типы update в телеграм
 */

public enum TelegramUpdateType {
    MESSAGE,
    EDITED_MESSAGE,
    CALLBACK_QUERY,
    INLINE_QUERY,
    CHAT_MEMBER,
    MY_CHAT_MEMBER,
    CHANNEL_POST,
    UNKNOWN;

    public static TelegramUpdateType getType(Update update) {
        if (update.hasMessage()) return TelegramUpdateType.MESSAGE;
        if (update.hasEditedMessage()) return TelegramUpdateType.EDITED_MESSAGE;
        if (update.hasCallbackQuery()) return TelegramUpdateType.CALLBACK_QUERY;
        if (update.hasInlineQuery()) return TelegramUpdateType.INLINE_QUERY;
        if (update.hasChatMember()) return TelegramUpdateType.CHAT_MEMBER;
        if (update.hasMyChatMember()) return TelegramUpdateType.MY_CHAT_MEMBER;
        if (update.hasChannelPost()) return TelegramUpdateType.CHANNEL_POST;
        return TelegramUpdateType.UNKNOWN;
    }

}
