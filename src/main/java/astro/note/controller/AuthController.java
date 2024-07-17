package astro.note.controller;

import astro.note.Repository.AccountActivationTokenRepository;
import astro.note.Repository.UserRepository;
import astro.note.entity.AccountActivationToken;
import astro.note.entity.User;
import astro.note.service.Interface.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountActivationTokenRepository accountActivationTokenRepository;

    @GetMapping("/login")
    public ModelAndView login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return new ModelAndView("redirect:/home");
        }
        System.out.println(authentication);
        System.out.println(authentication.getAuthorities());
        System.out.println(authentication instanceof AnonymousAuthenticationToken);
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
    @GetMapping("/register/confirm")
    public String showConfirmRegistration() {
        return "auth/confirmRegistration";
    }

    @GetMapping("/activate")
    public String activateAccount(@RequestParam("token") String token, Model model) {
        Optional<AccountActivationToken> optionalActivationToken = accountActivationTokenRepository.findByToken(token);

        if (optionalActivationToken.isEmpty() || optionalActivationToken.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("message", "Token is invalid or expired.");
        } else {
            AccountActivationToken activationToken = optionalActivationToken.get();
            User user = activationToken.getUser();
            user.setActive(1);
            userRepository.save(user);
            accountActivationTokenRepository.delete(activationToken);
            model.addAttribute("message", "Your account has been successfully activated.");
        }
        return "auth/activation";
    }

    /* Rest Password
    * Checks Principal object to see if user is logged in
    * If user is logged in, it retrieves the username from the Principal object
    * It then uses the username to retrieve the User object from the database
    * It then adds the User object to the model and returns the resetPassword view
    * If user is not logged in, it redirects to the login page
     */
    @GetMapping("/reset-password")
    public String resetPassword(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userService.findByUsername(username);
            model.addAttribute("user", user);
            return "auth/resetPassword";
        }
        return "redirect:/auth/login";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("currentPassword") String currentPassword,
                                @RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser();
        if (user!=null){
            if (!passwordEncoder.matches(currentPassword, user.getPassword())){
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect.");
                return "redirect:/auth/reset-password";
            }
            if (!password.equals(confirmPassword)){
                redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
                return "redirect:/auth/reset-password";
            }
            user.setPassword(passwordEncoder.encode(password));
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "Password updated successfully.");
            return "redirect:/user/information";
        }else {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/auth/login";
        }
    }

}
