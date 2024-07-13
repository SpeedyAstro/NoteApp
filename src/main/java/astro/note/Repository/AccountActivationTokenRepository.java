package astro.note.Repository;

import astro.note.entity.AccountActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AccountActivationTokenRepository extends JpaRepository<AccountActivationToken,Long> {
    Optional<AccountActivationToken> findByToken(String token);
    @Transactional
    @Modifying
    @Query("DELETE FROM AccountActivationToken t WHERE t.expiryDate <= ?1")
    void deleteAllExpiredSince(LocalDateTime now);
}
