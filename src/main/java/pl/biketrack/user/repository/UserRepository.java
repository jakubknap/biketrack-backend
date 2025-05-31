package pl.biketrack.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.biketrack.user.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query(value = """
            SELECT EXISTS(SELECT 1
                          FROM _user u
                          WHERE u.email = :email
                             OR u.nickname = :nickname)
            """, nativeQuery = true)
    boolean existsByEmailOrNickname(String email, String nickname);

    @Query(value = """
            SELECT EXISTS(SELECT 1
                          FROM _user u
                          WHERE u.email = :email
                             AND u.uuid != :userUuid)
            """, nativeQuery = true)
    boolean isEmailTaken(String email, UUID userUuid);

    @Query(value = """
            SELECT EXISTS(SELECT 1
                          FROM _user u
                          WHERE u.nickname = :nickname
                             AND u.uuid != :userUuid)
            """, nativeQuery = true)
    boolean isNicknameTaken(String nickname, UUID userUuid);
}