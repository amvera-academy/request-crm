package avishgreen.amvera.crm.repositories;

import avishgreen.amvera.crm.entities.SupportRequest;
import avishgreen.amvera.crm.enums.SupportRequestStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {

    List<SupportRequest> findByAuthorIdAndChatIdAndStatusNotIn(Long authorId, Long chatId, Collection<SupportRequestStatusType> statuses);
    // Method to find requests by a single status
    List<SupportRequest> findByStatusOrderByLastMessageAtDesc(SupportRequestStatusType status);

    // Method to find requests by a collection of statuses
    List<SupportRequest> findByStatusInOrderByLastMessageAtDesc(Set<SupportRequestStatusType> statuses);

    List<SupportRequest> findByAuthorIdAndIdNotOrderByLastMessageAtDesc(Long authorId, Long currentRequestId);
}