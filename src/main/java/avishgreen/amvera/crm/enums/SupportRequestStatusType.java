package avishgreen.amvera.crm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SupportRequestStatusType {
    REQUIRES_ATTENTION("Требует внимания"),
    ANSWERED("Отвечен"),
    UNANSWERED("Не отвечен"),
    COMPLETED("Завершен"),
    IGNORE("Игнорировать");

    private final String displayName;
}