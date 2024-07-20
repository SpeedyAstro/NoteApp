package astro.note.Repository;

import astro.note.entity.Group;
import astro.note.entity.GroupNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GroupNoteRepository extends JpaRepository<GroupNote,Long> {
    List<GroupNote> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    @Transactional
    @Modifying
    @Query("DELETE FROM GroupNote gn WHERE gn.group.id = :groupId")
    void deleteAllNotesByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT gn FROM GroupNote gn WHERE gn.group = :group AND gn.noteContent LIKE CONCAT(:noteContent, '%')")
    List<GroupNote> findByGroupAndNoteContentStartingWith(@Param("group") Group group, @Param("noteContent") String noteContent);

}
