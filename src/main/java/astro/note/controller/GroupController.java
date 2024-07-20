package astro.note.controller;

import astro.note.DTO.GroupDto;
import astro.note.DTO.UserDto;
import astro.note.Repository.GroupMemberRepository;
import astro.note.Repository.GroupNoteRepository;
import astro.note.Repository.GroupRepository;
import astro.note.Repository.UserRepository;
import astro.note.entity.Group;
import astro.note.entity.GroupMember;
import astro.note.entity.GroupNote;
import astro.note.entity.User;
import astro.note.service.ExcelService;
import astro.note.service.Interface.GroupNoteService;
import astro.note.service.Interface.GroupService;
import astro.note.service.Interface.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Controller
@RequestMapping("/group")
public class GroupController {
    private final GroupService groupService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final GroupNoteRepository groupNoteRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExcelService excelService;
    private final GroupNoteService groupNoteService;
    private final GroupRepository groupRepository;

    @Autowired
    public GroupController(GroupService groupService, UserService userService, UserRepository userRepository, GroupNoteRepository groupNoteRepository, GroupMemberRepository groupMemberRepository, ExcelService excelService, GroupNoteService groupNoteService, GroupRepository groupRepository) {
        this.groupService = groupService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.groupNoteRepository = groupNoteRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.excelService = excelService;
        this.groupNoteService = groupNoteService;
        this.groupRepository = groupRepository;
    }

    private static final Logger logger = Logger.getLogger(GroupController.class.getName());

    private String generateGroupCode() {
        return String.valueOf((int) (Math.random() * 9000) + 1000);
    }
    @GetMapping("/choice")
    public String showGroupChoice(Model model, HttpSession session, Principal principal, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size, RedirectAttributes redirectAttributes) {
        System.out.println("GroupController.showGroupChoice");
        String username = principal.getName();
        User user = userRepository.findByUsername(username);

        String groupCode = generateGroupCode();
        session.setAttribute("groupCode", groupCode);
        model.addAttribute("groupCode", groupCode);

        if (user != null) {
            List<Group> myGroups = groupService.findGroupsByCreatorOrderByCreatedAtDesc(user);
            List<Group> joinedGroups = groupMemberRepository.findGroupsByUser(user);
            joinedGroups.removeIf(group -> group.getCreator().equals(user));

            int totalGroups = myGroups.size();
            int totalPages = (int) Math.ceil((double) totalGroups / size);

            int start = (page - 1) * size;
            int end = Math.min(start + size, totalGroups);
            List<Group> paginatedGroups = myGroups.subList(start, end);

            model.addAttribute("myGroups", paginatedGroups);
            model.addAttribute("joinedGroups", joinedGroups);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("size", size);
        }
        return "teamNote/choice";
    }

    @GetMapping("/create")
    public String showCreateGroupPage(Model model, HttpSession session) {
        String groupCode = (String) session.getAttribute("groupCode");
        model.addAttribute("groupCode", groupCode);
        return "teamNote/create";
    }
    @PostMapping("/create")
    public String createGroup(@RequestParam String groupName, @RequestParam String groupCode, Principal principal, RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "redirect:/auth/login";
        }
        Group group = new Group();
        group.setGroupName(groupName);
        group.setGroupCode(groupCode);
        group.setCreator(user);
        groupService.saveGroup(group);

        redirectAttributes.addFlashAttribute("successMessage", "Congrats! Note updated successfully.");
        return "redirect:/group/choice";
    }
    @GetMapping("/details/{id}")
    public String showGroupDetails(@PathVariable Long id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Group group = groupService.findById(id);
        if (group == null) {
            return "redirect:/group/choice";
        }
        User creator = group.getCreator();
        String username = principal.getName();
        User currentUser = userRepository.findByUsername(username);
        String role;
        boolean isBlocked = false;

        if (currentUser != null) {
            GroupMember groupMember = groupMemberRepository.findByUserAndGroup(currentUser, group);
            if (groupMember != null) {
                isBlocked = groupMember.isBlocked();
            }
        }

        if (currentUser != null && currentUser.equals(creator)) {
            role = "Administrator";
        } else {
            role = "Member";
        }

        List<GroupMember> groupMembers = groupMemberRepository.findGroupMembersWithUsersByGroupId(id);
        int memberCount = groupMembers.size();
        int noteCount = group.getNotes().size();

        GroupDto groupDto = GroupDto.from(group);

        model.addAttribute("group", groupDto);
        model.addAttribute("creator", creator);
        model.addAttribute("groupMembers", groupMembers);
        model.addAttribute("memberCount", memberCount);
        model.addAttribute("noteCount", noteCount);
        model.addAttribute("role", role);
        model.addAttribute("isBlocked", isBlocked);

        if (isBlocked) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã bị chặn khỏi nhóm này.");
        }

        return "teamNote/groupDetails";
    }

    @PostMapping("/join")
    public String joinGroup(@RequestParam("groupCode") String groupCode, Model model, Principal principal, RedirectAttributes redirectAttributes){
        Group group = groupRepository.findByGroupCode(groupCode);
        if (group != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username);
            if (user != null){
                GroupMember existingGroupMember = groupMemberRepository.findByUserAndGroup(user, group);
                if (existingGroupMember == null){
                    GroupMember groupMember = new GroupMember();
                    groupMember.setGroup(group);
                    groupMember.setUser(user);
                    groupMemberRepository.save(groupMember);
                    return "redirect:/group/list?groupCode=" + groupCode;
                } else if (existingGroupMember.isBlocked()){
                    redirectAttributes.addFlashAttribute("errorMessage", "You have been blocked from this group.");
                    return "redirect:/group/choice";
                }else {
                    return "redirect:/group/list?groupCode=" + groupCode;
                }
            }else {
                redirectAttributes.addFlashAttribute("errorMessage", "User does not exist.");
                return "redirect:/group/choice";
            }
        }else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid Token");
            return "redirect:/group/choice";
        }
    }

    @GetMapping("/list")
    public String showGroupList(@RequestParam("groupCode") String groupCode, Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username);
        if (user != null) {
            Group group = groupRepository.findByGroupCode(groupCode);
            if (group != null) {
                GroupMember groupMember = groupMemberRepository.findByUserAndGroup(user, group);
                if (groupMember != null && groupMember.isBlocked()) {
                    model.addAttribute("errorMessage", "You have been blocked from this group.");
                    return "redirect:/group/choice";
                }
                return getAllGroupNote(group.getId(), model, principal);
            } else {
                model.addAttribute("errorMessage", "Group does not exist.");
                return "redirect:/group/choice";
            }
        } else {
            model.addAttribute("errorMessage", "User does not exist.");
            return "redirect:/group/choice";
        }
    }

    @GetMapping("/listNotes")
    public String getAllGroupNote(@RequestParam("groupId") Long groupId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username);
        if (user != null) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid group ID"));

            GroupDto groupDto = GroupDto.from(group);
            UserDto userDto = UserDto.from(user);

            model.addAttribute("user", userDto);
            model.addAttribute("fullName", user.getFullName());
            model.addAttribute("group", groupDto);

            List<GroupNote> notes = groupNoteRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
            model.addAttribute("teamNotes", notes);

            return "teamNote/index";
        } else {
            return "redirect:/auth/login";
        }
    }

    /*
    * Add note form
    * Bind GroupNote object to the form to get the note details
     */
    @GetMapping("/addNote")
    public String addNoteForm(Model model, @RequestParam("groupId") Long groupId) {
        model.addAttribute("groupNote", new GroupNote());
        model.addAttribute("groupId", groupId);
        return "teamNote/addNote";
    }

    @PostMapping("/saveNote")
    public String saveNote(@ModelAttribute GroupNote groupNote, @RequestParam("groupId") Long groupId, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username);
        if (user != null) {
            Group group = groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Invalid group ID"));
            groupNote.setGroup(group);
            groupNote.setUser(user);
            groupNote.setCreatedAt(LocalDateTime.now());
            groupNoteRepository.save(groupNote);
        } else {
            return "redirect:/login";
        }
        return "redirect:/group/listNotes?groupId=" + groupId;
    }

    @GetMapping("/search/findByGroupNoteContentStartingWith/{groupId}/{content}")
    public String getGroupNoteByContent(@PathVariable("groupId") Long groupId, @PathVariable("content") String content, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        User user = userRepository.findByUsername(username);

        if (user != null) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid group ID"));

            List<GroupNote> notes = groupNoteRepository.findByGroupAndNoteContentStartingWith(group, content);
            if (notes.isEmpty()) {
                model.addAttribute("message", "No notes found with content " + content);
            } else {
                model.addAttribute("teamNotes", notes);
            }
            model.addAttribute("group", GroupDto.from(group));
            model.addAttribute("fullName", user.getFullName());
        }
        return "teamNote/index";
    }

    @PostMapping("note/delete-all")
    public String deleteAllGroupNotes(@RequestParam("groupId") Long groupId, Model model) {
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            model.addAttribute("error", "Group does not exist.");
            return "redirect:/group/list";
        }
        groupNoteService.deleteAllNotesByGroupId(groupId);
        return "redirect:/group/list?groupCode=" + group.getGroupCode();
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadGroupNotes(@RequestParam("groupId") Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid group ID"));
        List<GroupNote> notes = groupNoteRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        ByteArrayInputStream in = excelService.exportGroupNotesToExcel(notes);

        HttpHeaders headers = new HttpHeaders();
        String filename = "group_notes_" + group.getGroupCode() + ".xls";
        headers.add("Content-Disposition", "attachment; filename=" + filename);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }

    @GetMapping("/note/read/{id}")
    public String showGroupNoteDetails(@PathVariable Long id, Model model, Principal principal) {
        GroupNote groupNote = groupNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid group note ID"));

        if (principal != null) {
            String username = principal.getName();
            User user = userService.findByUsername(username);
            UserDto userDto = UserDto.from(user);
            model.addAttribute("user", userDto);
        }

        Group group = groupNote.getGroup();
        GroupDto groupDto = GroupDto.from(group);
        model.addAttribute("group", groupDto);
        model.addAttribute("groupNote", groupNote);
        return "teamNote/groupNoteDetails";
    }

    @PostMapping("/note/delete/{noteId}")
    public String deleteGroupNote(@PathVariable Long noteId, @RequestParam("groupId") Long groupId, RedirectAttributes redirectAttributes) {
        GroupNote groupNote = groupNoteRepository.findById(noteId).orElse(null);
        if (groupNote == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ghi chú không tồn tại.");
            return "redirect:/group/listNotes?groupId=" + groupId;
        }
        groupNoteRepository.delete(groupNote);
        redirectAttributes.addFlashAttribute("successMessage", "Ghi chú đã được xóa thành công!");
        return "redirect:/group/listNotes?groupId=" + groupId;
    }
}
