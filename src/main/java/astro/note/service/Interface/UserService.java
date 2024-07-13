package astro.note.service.Interface;

import astro.note.entity.User;

public interface UserService {
    void registerUser(User user);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User findByUsername(String name);

    User getCurrentUser();

    void updateUser(User user);

    void saveUser(User user);

    boolean isEmailUnique(String email, Long userId);

    String forgotPasswordUseEmail(String email) throws Exception;

    String resetPasswordUseEmail(String token, String newPassword) throws Exception;

    void save(User user);
}
