package astro.note.controller;

import astro.note.entity.User;
import astro.note.service.Interface.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public ModelAndView login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return new ModelAndView("redirect:/home");
        }
        ModelAndView modelAndView = new ModelAndView("auth/login");
        if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DELETED"))) {
            modelAndView.addObject("error", "Your account has been deleted.");
        }
        return modelAndView;
    }
    @GetMapping("/register/create")
    public String showRegistrationForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String && auth.getPrincipal().equals("anonymousUser"))) {
            return "redirect:/home";
        }
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register/save")
    public String registerAccount(@ModelAttribute("user") User user) {
        try {
            userService.registerUser(user);
            return "redirect:/loading-for-register";
        } catch (RuntimeException e) {
            return "redirect:/auth/register/create?error=true";
        }
    }
}
