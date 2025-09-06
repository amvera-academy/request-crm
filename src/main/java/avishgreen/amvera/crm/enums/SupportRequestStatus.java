package avishgreen.amvera.crm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SupportRequestStatus {
    ANSWERED("Отвечен"),
    UNANSWERED("Не отвечен"),
    COMPLETED("Завершен"),
    REQUIRES_ATTENTION("Требует внимания"),
    IGNORE("Игнорировать");

    private final String displayName;
}