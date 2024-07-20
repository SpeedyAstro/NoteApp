package astro.note.controller;

import astro.note.DTO.NoteDto;
import astro.note.DTO.UserDto;
import astro.note.Repository.NoteRepository;
import astro.note.Repository.UserRepository;
import astro.note.entity.Note;
import astro.note.entity.User;
import astro.note.service.ExcelService;
import astro.note.service.Interface.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/note")
public class NoteController {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final NoteService noteService;
    private final ExcelService excelService;

    @Autowired
    public NoteController(UserRepository userRepository, NoteRepository noteRepository, NoteService noteService, ExcelService excelService) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.noteService = noteService;
        this.excelService = excelService;
    }

    private Optional<User> getAuthenticatedUser(Principal principal) {
        if (principal == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userRepository.findByUsername(principal.getName()));
    }

    // Show all notes
    @GetMapping("/list")
    public String getAllNote(Model model, Principal principal) {
        System.out.println("NoteController.getAllNote");
        Optional<User> optionalUser = getAuthenticatedUser(principal);
        if (optionalUser.isEmpty()) {
            return "redirect:/auth/login";
        }

        User user = optionalUser.get();
        UserDto userDto = UserDto.from(user);
        model.addAttribute("user", userDto);
        model.addAttribute("fullName", user.getFullName());
        List<Note> notes = noteRepository.findActiveNotesByUserIdOrderByTimeDesc(user.getId());

        List<NoteDto> noteDtos = notes.stream()
                .map(NoteDto::from)
                .collect(Collectors.toList());

        model.addAttribute("notes", noteDtos);
        return "note/index";
    }

    /*
    Create a new note
    Checks Authentication
    On success, add the user (from context) and note to the model and return the addNote template
     */
    @GetMapping("/create")
    public String create(Model model, Principal principal) {
        Optional<User> optionalUser = getAuthenticatedUser(principal);
        if (optionalUser.isEmpty()) {
            return "redirect:/auth/login";
        }

        User user = optionalUser.get();
        UserDto userDto = UserDto.from(user);
        model.addAttribute("user", userDto);
        model.addAttribute("note", new Note());
        return "note/addNote";
    }

    // Save New Note
    @PostMapping("/save")
    public String saveNote(@ModelAttribute("note") Note note, Principal principal) {
        Optional<User> optionalUser = getAuthenticatedUser(principal);
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();
        noteService.addNote(note, user);
        return "redirect:/note/list";
    }

    @PostMapping("/move-all-to-trash")
    public String moveAllNotesToTrash(Principal principal) {
        Optional<User> optionalUser = getAuthenticatedUser(principal);
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();
        List<Note> notes = noteRepository.findByUserAndIsDeletedFalse(user);
        for (Note note : notes) {
            note.setDeleted(true);
            note.setDeletedAt(LocalDateTime.now());
            noteRepository.save(note);
        }

        return "redirect:/note/list";
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadNotes() {
        List<Note> notes = noteService.getAllActiveNotes();
        ByteArrayInputStream in = excelService.exportNotesToExcel(notes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=myNotes.xls");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }

    @PostMapping("/move-to-trash/{id}")
    public String moveToTrash(@PathVariable Long id) {
        noteService.moveToTrash(id);
        return "redirect:/note/list";
    }

    @GetMapping("/read/{id}")
    public String readNote(@PathVariable Long id, Model model, Principal principal) {
        Optional<User> optionalUser = getAuthenticatedUser(principal);
        if (optionalUser.isEmpty()) {
            return "redirect:/auth/login";
        }

        User user = optionalUser.get();
        Note note = noteService.getNoteById(id);

        if (note == null || !note.getUser().equals(user)) {
            return "redirect:/note/list";
        }

        UserDto userDto = UserDto.from(user);
        model.addAttribute("user", userDto);
        NoteDto noteDto = NoteDto.from(note);
        model.addAttribute("note", noteDto);
        return "note/read";
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Principal principal) {
        Optional<User> optionalUser = getAuthenticatedUser(principal);
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();
        Note note = noteService.getNoteById(id);

        if (note == null || !note.getUser().equals(user)) {
            return "redirect:/note/list";
        }

        UserDto userDto = UserDto.from(user);
        model.addAttribute("user", userDto);
        model.addAttribute("note", note);
        return "note/editNote";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("note") Note note, Model model, Principal principal) {
        Optional<User> optionalUser = getAuthenticatedUser(principal);
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();
        Note existingNote = noteService.getNoteById(id);

        if (existingNote == null || !existingNote.getUser().equals(user)) {
            return "redirect:/note/list";
        }

        existingNote.setTitle(note.getTitle());
        existingNote.setContent(note.getContent());
        existingNote.setTime(LocalDateTime.now());

        noteService.updateNote(existingNote);
        return "redirect:/note/list";
    }
}
