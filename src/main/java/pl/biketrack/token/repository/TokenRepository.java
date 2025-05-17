package pl.biketrack.token.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pl.biketrack.token.dto.TokenStatusAndType;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.model.Token;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Modifying
    @Transactional
    @Query("""
            UPDATE Token t
            SET t.revoked = TRUE
            WHERE t.revoked = FALSE
              AND t.user.uuid = :userUuid
              AND t.tokenType IN :tokenTypes
            """)
    void revokeAllValidTokensByUserUuid(UUID userUuid, List<TokenType> tokenTypes);

    @Query("""
            SELECT new pl.biketrack.token.dto.TokenStatusAndType(t.revoked, t.tokenType)
            FROM Token t
            WHERE t.token = :token
            """)
    Optional<TokenStatusAndType> getTokenStatusAndType(String token);

    @Query("""
            SELECT u.uuid
            FROM Token t
                     INNER JOIN User u ON t.user.id = u.id
            WHERE t.token = :token
            """)
    UUID findUserUuidByToken(String token);
}