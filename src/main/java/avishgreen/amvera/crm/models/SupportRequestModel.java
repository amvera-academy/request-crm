package avishgreen.amvera.crm.models;

import avishgreen.amvera.crm.dto.SupportMessageDto;
import avishgreen.amvera.crm.enums.SupportRequestStatus;
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
    private List<SupportMessageDto> messages; // Теперь это список сообщений
    private String author;
    private List<String> participants;
    private LocalDateTime lastUpdateTime;
    private String note;
    private SupportRequestStatus status;
}