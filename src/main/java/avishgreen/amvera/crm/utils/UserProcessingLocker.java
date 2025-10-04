package avishgreen.amvera.crm.utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
// Этот компонент будет хранить и предоставлять блокировки для каждого пользователя
public class UserProcessingLocker {

    // Ключ: ID пользователя, Значение: Блокировка (мьютекс) для этого пользователя
    private final ConcurrentHashMap<Long, Lock> userLocks = new ConcurrentHashMap<>();

    /**
     * Предоставляет потокобезопасный мьютекс для конкретного User ID.
     * Если мьютекс уже существует, возвращает его. Если нет — создает и возвращает.
     */
    public Lock getLockForUser(Long userId) {
        // computeIfAbsent атомарно создает и кладет ReentrantLock, если его еще нет
        return userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
    }

    /**
     * Очищает мьютекс после завершения обработки, чтобы избежать утечек памяти.
     * Вызывать после unlock()
     */
    public void removeLockForUser(Long userId) {
        // Важно: Удалять нужно только после того, как lock был разблокирован.
        // Если вы хотите избежать гонки между удалением и получением, можно использовать
        // более сложный механизм, но для большинства случаев простое удаление сработает.
        userLocks.remove(userId);
    }
}