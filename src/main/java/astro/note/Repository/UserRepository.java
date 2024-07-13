package astro.note.Repository;

import astro.note.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    Optional<User> findById(Long id);
    List<User> findByUsernameContaining(String username);
    List<User> findByFullNameContaining(String fullName);
    List<User> findByEmailContaining(String email);
}
