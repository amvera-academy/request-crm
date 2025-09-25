package avishgreen.amvera.crm.repositories;

import avishgreen.amvera.crm.entities.TelegramMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramMessageRepository extends JpaRepository<TelegramMessage, Integer> {
    Optional<TelegramMessage> findTopBySupportRequestIdOrderBySentAtDesc(Long supportRequestId);
    Optional<TelegramMessage> findByTelegramMessageId(Integer id);
}