package avishgreen.amvera.crm.exceptions;

import java.io.IOException;

public class TelegramFileNotFoundException extends IOException {
    public TelegramFileNotFoundException(String message) {
        super(message);
    }
}