package pl.biketrack.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.biketrack.user.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query(value = """
            SELECT EXISTS(SELECT 1
                          FROM _user u
                          WHERE u.email = :email
                             or u.nickname = :nickname)
            """, nativeQuery = true)
    boolean existsByEmailOrNickname(String email, String nickname);
}