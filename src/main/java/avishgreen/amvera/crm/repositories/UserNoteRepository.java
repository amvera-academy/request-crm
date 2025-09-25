package avishgreen.amvera.crm.repositories;

import avishgreen.amvera.crm.entities.UserNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNoteRepository extends JpaRepository<UserNote, Long> {
    List<UserNote> findByAuthorId(Long authorId);
}