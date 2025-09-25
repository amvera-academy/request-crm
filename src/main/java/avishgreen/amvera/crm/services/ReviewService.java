package avishgreen.amvera.crm.services;

import avishgreen.amvera.crm.dto.SupportRequestDto;
import avishgreen.amvera.crm.dto.SupportRequestReviewDto;
import avishgreen.amvera.crm.enums.SupportRequestStatusType;
import avishgreen.amvera.crm.mappers.SupportRequestMapper;
import avishgreen.amvera.crm.mappers.SupportRequestReviewMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Getter
@RequiredArgsConstructor
public class ReviewService {
    private final SupportRequestService supportRequestService;
    private final SupportRequestMapper supportRequestMapper;
    private final SupportRequestReviewMapper supportRequestReviewMapper;

    @Transactional
    public void updateRequestStatus(Long id, SupportRequestStatusType status) {
        var supportRequest = supportRequestService.getSupportRequestById(id);
        supportRequest.setStatus(status);
    }

    public SupportRequestDto getSupportRequestDtoById(Long id) {
        var supportRequest = supportRequestService.getSupportRequestByIdWithMessages(id);
        return supportRequestMapper.toDto(supportRequest);
    }

    public List<SupportRequestReviewDto> getUnansweredRequests() {
        return supportRequestService.getRequestsByStatus(SupportRequestStatusType.UNANSWERED).stream()
                .map(supportRequestReviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<SupportRequestReviewDto> getRequiresAttentionRequests() {
        return supportRequestService.getRequestsByStatus(SupportRequestStatusType.REQUIRES_ATTENTION).stream()
                .map(supportRequestReviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<SupportRequestReviewDto> getArchivedRequests() {
        Set<SupportRequestStatusType> archivedStatuses = EnumSet.of(
                SupportRequestStatusType.COMPLETED,
                SupportRequestStatusType.IGNORE,
                SupportRequestStatusType.ANSWERED
        );
        return supportRequestService.getRequestsByStatuses(archivedStatuses).stream()
                .map(supportRequestReviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<SupportRequestDto> getPreviousRequestsByAuthor(Long authorId, Long currentRequestId) {
        return supportRequestService.getPreviousRequestsByAuthor(authorId, currentRequestId)
                .stream()
                .map(supportRequestMapper::toDto)
                .collect(Collectors.toList());
    }

}