package astro.note.controller;

import astro.note.DTO.UserDto;
import astro.note.entity.User;
import astro.note.service.Interface.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping()
public class HomeController {

    @Autowired
    private UserService userService;
    @GetMapping("/")
    public String homePage() {
        return "home/index";
    }


    @GetMapping("/home")
    public String home(Principal principal, Model model) {
        if (principal != null) {
            String username = principal.getName();
            User user = userService.findByUsername(username);
            model.addAttribute("fullName", user.getFullName());
            UserDto userDto = UserDto.from(user);
            model.addAttribute("user", userDto);
        }
        return "home/index";
    }
}
