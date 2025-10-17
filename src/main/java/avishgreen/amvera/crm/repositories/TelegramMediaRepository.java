package avishgreen.amvera.crm.repositories;

import avishgreen.amvera.crm.entities.TelegramMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для управления сущностями TelegramMedia.
 * Расширяет JpaRepository для предоставления стандартных CRUD-операций.
 */
@Repository
public interface TelegramMediaRepository extends JpaRepository<TelegramMedia, Long> {
    // Поскольку сервис использует только save(), дополнительных методов здесь пока не требуется.
}
