package astro.note.controller;

import astro.note.entity.User;
import astro.note.service.Interface.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentController {
    @Autowired
    private UserService userService;

    @GetMapping("user/register-vip")
    public String registerVip(Model model, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User user = userService.findByUsername(username);
        String role = user.getRole();

        boolean isVip = role.contains("ROLE_VIPMEMBER") || role.contains("ROLE_ADMIN") || role.contains("ROLE_MANAGER");

        model.addAttribute("isVip", isVip);
        model.addAttribute("username", username);
        model.addAttribute("selectedPackage", "gold");

        return "payment/register-vip";
    }
}
