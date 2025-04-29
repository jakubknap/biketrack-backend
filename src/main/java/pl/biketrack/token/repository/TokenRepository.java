package pl.biketrack.token.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.biketrack.token.enumerated.TokenType;
import pl.biketrack.token.model.Token;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
            SELECT t
            FROM Token t
                     INNER JOIN User u ON t.user.id = u.id
            WHERE t.revoked = FALSE
              AND u.uuid = :userUuid
              AND t.tokenType IN :tokenTypes
            """)
    List<Token> findAllValidTokensForUserByType(UUID userUuid, List<TokenType> tokenTypes);

    Optional<Token> findByToken(String token);

    @Query("""
            SELECT t
            FROM Token t
                     INNER JOIN User u ON t.user.id = u.id
            WHERE t.token = :token
            """)
    Optional<Token> findByTokenWithUser(String token);
}