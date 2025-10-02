package avishgreen.amvera.crm.repositories;

import avishgreen.amvera.crm.entities.TelegramUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tu FROM TelegramUser tu WHERE tu.id = :id")
    Optional<TelegramUser> findAndLockById(Long id);

}