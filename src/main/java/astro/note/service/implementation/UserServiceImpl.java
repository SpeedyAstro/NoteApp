package astro.note.service.implementation;

import astro.note.Repository.AccountActivationTokenRepository;
import astro.note.Repository.PasswordResetTokenRepository;
import astro.note.Repository.UserRepository;
import astro.note.entity.AccountActivationToken;
import astro.note.entity.PasswordResetToken;
import astro.note.entity.User;
import astro.note.service.EmailService;
import astro.note.service.Interface.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Value("${activation.link}")
    private String ActivationLink;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private AccountActivationTokenRepository accountActivationTokenRepository;
    @Override
    @Transactional
    public void registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("User Already Exists");
        }
        user.setRole("ROLE_USER");
        user.setActive(0);
        user.setRegistrationTime(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.saveAndFlush(user);
        String token = UUID.randomUUID().toString();
        AccountActivationToken activationToken = new AccountActivationToken();
        activationToken.setToken(token);
        activationToken.setUser(user);
        activationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
        accountActivationTokenRepository.save(activationToken);
        String activationLink = ActivationLink + token;
        String subject = "Activate your account";
        String text = "Congratulations " + user.getFullName() + " has successfully registered your account " + user.getUsername() + ", click this link to activate your account: " + activationLink;

        emailService.sendEmail(user.getEmail(), subject, text);
        System.out.println("Email sent to " + user.getEmail() + " with activation link " + activationLink);
    }

    @Override
    public boolean existsByUsername(String user) {
        return userRepository.findByUsername(user) != null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email) != null;
    }

    @Override
    public User findByUsername(String name) {
        return userRepository.findByUsername(name);
    }


    @Override
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username);
        }
        return null;
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public boolean isEmailUnique(String email, Long userId) {
        User user = userRepository.findByEmail(email);
        return user == null || user.getId().equals(userId);
    }

    @Override
    public String forgotPasswordUseEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found.");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByUser(user);
        if (passwordResetToken == null) {
            passwordResetToken = new PasswordResetToken();
            passwordResetToken.setUser(user);
        }
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiryDate(expiryDate);
        passwordResetTokenRepository.save(passwordResetToken);

        String resetUrl = "http://localhost:8080/auth/reset-password-form?token=" + token;
        String subject = "Password Reset Request";
        String content = "Click here to recover your account password " + user.getUsername() +" + user.getFullName() :"  + resetUrl;

        emailService.sendEmail(user.getEmail(), subject, content);

        return "Password reset email sent.";
    }

    @Override
    public String resetPasswordUseEmail(String token, String newPassword) throws Exception {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid, you have just changed the password."));
        if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired.");
        }
        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(passwordResetToken);

        return "Password reset successful.";
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }
}
