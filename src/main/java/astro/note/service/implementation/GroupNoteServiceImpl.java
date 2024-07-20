package astro.note.service.implementation;

import astro.note.Repository.GroupNoteRepository;
import astro.note.entity.GroupNote;
import astro.note.service.Interface.GroupNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupNoteServiceImpl implements GroupNoteService {
    private final GroupNoteRepository groupNoteRepository;

    @Autowired
    public GroupNoteServiceImpl(GroupNoteRepository groupNoteRepository) {
        this.groupNoteRepository = groupNoteRepository;
    }

    @Override
    public List<GroupNote> getAllGroupNotes() {
        return groupNoteRepository.findAll();
    }

    @Override
    public Optional<GroupNote> getGroupNoteById(Long id) {
        return groupNoteRepository.findById(id);
    }

    @Override
    public GroupNote saveGroupNote(GroupNote groupNote) {
        return groupNoteRepository.save(groupNote);
    }

    @Override
    public void deleteGroupNote(Long id) {
        groupNoteRepository.deleteById(id);
    }

    @Override
    public void deleteAllNotesByGroupId(Long groupId) {
        groupNoteRepository.deleteAllNotesByGroupId(groupId);
    }

    @Override
    public void updateGroupNote(GroupNote existingGroupNote) {
        groupNoteRepository.save(existingGroupNote);
    }
}
