package avishgreen.amvera.crm.models;

import avishgreen.amvera.crm.dto.MessageToSupportDto;
import avishgreen.amvera.crm.enums.SupportRequestStatusType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SupportRequestModel {
    private Long id;
    private List<MessageToSupportDto> messages; // Теперь это список сообщений
    private String author;
    private List<String> participants;
    private LocalDateTime lastUpdateTime;
    private String note;
    private SupportRequestStatusType status;
}