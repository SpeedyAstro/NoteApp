package astro.note.service.Interface;

import astro.note.entity.GroupNote;

import java.util.List;
import java.util.Optional;

public interface GroupNoteService {
    List<GroupNote> getAllGroupNotes();
    Optional<GroupNote> getGroupNoteById(Long id);
    GroupNote saveGroupNote(GroupNote groupNote);
    void deleteGroupNote(Long id);
    void deleteAllNotesByGroupId(Long groupId);
    void updateGroupNote(GroupNote existingGroupNote);
}
