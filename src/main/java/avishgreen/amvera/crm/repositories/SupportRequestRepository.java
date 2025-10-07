package avishgreen.amvera.crm.repositories;

import avishgreen.amvera.crm.entities.SupportRequest;
import avishgreen.amvera.crm.enums.SupportRequestStatusType;
import jakarta.persistence.QueryHint;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "BYPASS"),
            @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "BYPASS")
    })
    List<SupportRequest> findByAuthorIdAndChatIdAndStatusNotIn(Long authorId, Long chatId, Collection<SupportRequestStatusType> statuses);

    // Method to find requests by a single status
    List<SupportRequest> findByStatusOrderByLastMessageAtDesc(SupportRequestStatusType status);

    // Method to find requests by a collection of statuses
    List<SupportRequest> findByStatusInOrderByLastMessageAtDesc(Set<SupportRequestStatusType> statuses);

    List<SupportRequest> findByAuthorIdAndIdNotOrderByLastMessageAtDesc(Long authorId, Long currentRequestId);

    @Query("SELECT r FROM SupportRequest r WHERE r.id = ?1")
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "BYPASS") // отключаем кеш при поиске
    })
    Optional<SupportRequest> findByIdBypassingCache(Long id);

    List<SupportRequest> findByAuthorIdAndChatId(@NonNull Long id, Long chatId);
}