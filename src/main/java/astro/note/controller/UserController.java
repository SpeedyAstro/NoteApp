package astro.note.controller;

import astro.note.DTO.UserDto;
import astro.note.entity.User;
import astro.note.service.Interface.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/information")
    public String userInfo(Model model) {
        String username = getCurrentUsername();
        if (username != null) {
            User user = userService.findByUsername(username);
            UserDto userDto = UserDto.from(user);
            model.addAttribute("user", userDto);
            return "auth/UserInformation";
        }
        return "redirect:/auth/login";
    }

    @GetMapping("/edit")
    public String showEditForm(Model model) {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);
        return "auth/edit";
    }

    @PostMapping("/edit")
    public String updateUser(@RequestParam String fullName, @RequestParam String title, @RequestParam String email,
                             RedirectAttributes redirectAttributes, Model model) {
        System.out.println("Full name: " + fullName);
        System.out.println("Title: " + title);
        System.out.println("Email: " + email);
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);
        System.out.println("role"+user.getRole());
        if (!userService.isEmailUnique(email, user.getId())) {
            model.addAttribute("error", "Email already exists in the system.");
            model.addAttribute("fullName", fullName);
            model.addAttribute("title", title);
            model.addAttribute("email", email);
            return "auth/edit";
        }

        user.setFullName(fullName);
        user.setTitle(title);
        user.setEmail(email);
        userService.saveUser(user);

        redirectAttributes.addFlashAttribute("successMessage", "Update successful!");
        return "redirect:/user/information";
    }
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}
