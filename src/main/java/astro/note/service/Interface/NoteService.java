package astro.note.service.Interface;

import astro.note.entity.Note;
import astro.note.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NoteService {
    List<Note> getAllNotes();

    Note getNoteById(Long id);

    Note addNote(Note note, User user);

    Note updateNote(Note note);

    void deleteNoteById(Long id);

    void deleteAllNotesByUser(User user);

    List<Note> getNoteByContent(String content);

    List<Note> getNotesByUserId(Long id);

    Note findById(Long id);

    void deleteOldNotes();

    void restoreAllNotes();

    Note findNoteInTrashById(Long id);

    void moveToTrash(Long id);

    void save(Note note);

    void restoreAllNotesByUser(User user);

    void deleteAllNotesPermanently();

    boolean isTrashEmpty();

    List<Note> getAllActiveNotes();
}
