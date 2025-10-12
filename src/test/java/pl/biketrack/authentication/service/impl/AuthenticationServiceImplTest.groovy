package pl.biketrack.authentication.service.impl

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import pl.biketrack.authentication.dto.request.LoginRequest
import pl.biketrack.authentication.dto.request.RegisterRequest
import pl.biketrack.exception.dto.response.BaseResponse
import pl.biketrack.exception.exception.ServiceException
import pl.biketrack.mail.service.MailService
import pl.biketrack.properties.FrontendProperties
import pl.biketrack.security.JwtService
import pl.biketrack.token.model.Token
import pl.biketrack.token.repository.TokenRepository
import pl.biketrack.token.service.TokenService
import pl.biketrack.token.service.TokenServiceFactory
import pl.biketrack.user.mapper.UserMapper
import pl.biketrack.user.model.User
import pl.biketrack.user.repository.UserRepository
import pl.biketrack.user.service.UserService
import spock.lang.Specification

import static pl.biketrack.common.enumerated.ResponseCode.E03000
import static pl.biketrack.common.enumerated.ResponseCode.E03002
import static pl.biketrack.common.enumerated.ResponseCode.E03003
import static pl.biketrack.common.enumerated.ResponseCode.S00000
import static pl.biketrack.common.enumerated.ResponseCode.S00003
import static pl.biketrack.token.enumerated.TokenType.ACCESS_TOKEN
import static pl.biketrack.token.enumerated.TokenType.ACCOUNT_ACTIVATION_TOKEN
import static pl.biketrack.token.enumerated.TokenType.REFRESH_TOKEN
import static pl.biketrack.user.enumerated.UserStatus.ACTIVE
import static pl.biketrack.user.enumerated.UserStatus.REGISTERED

class AuthenticationServiceImplTest extends Specification {

    def userRepository = Mock(UserRepository)
    def userMapper = Mock(UserMapper)
    def userService = Mock(UserService)
    def jwtService = Mock(JwtService)
    def tokenRepository = Mock(TokenRepository)
    def authenticationManager = Mock(AuthenticationManager)
    def tokenServiceFactory = Mock(TokenServiceFactory)
    def mailService = Mock(MailService)
    def frontendProperties = Mock(FrontendProperties)

    def authService = new AuthenticationServiceImpl(userRepository, userMapper, userService, jwtService, tokenRepository, authenticationManager,
            tokenServiceFactory, mailService, frontendProperties)

    def "register should throw exception if user exists"() {
        given:
        def request = new RegisterRequest("nick", "test@example.com", "pass")
        userRepository.existsByEmailOrNickname(_ as String, _ as String) >> true

        when:
        authService.register(request)

        then:
        def ex = thrown(ServiceException)
        ex.getStatus() == E03000
    }

    def "register should save new user and send activation email"() {
        given:
        def request = new RegisterRequest("nick", "test@example.com", "pass")
        def user = new User(uuid: UUID.randomUUID(), email: request.email(), nickname: request.nickname())
        userRepository.existsByEmailOrNickname(_ as String, _ as String) >> false
        userMapper.mapToUserEntity(request) >> user
        def tokenService = Mock(TokenService)
        tokenServiceFactory.getTokenService(ACCOUNT_ACTIVATION_TOKEN) >> tokenService
        tokenService.generateToken(user) >> "token123"
        frontendProperties.prepareAccountActivationLink("token123") >> "https://link"

        when:
        def response = authService.register(request)

        then:
        1 * userRepository.save(user)
        1 * mailService.sendMailAsync(_)
        response instanceof BaseResponse
        response.status == S00003
    }

    def "activateAccount should activate user if status REGISTERED"() {
        given:
        def user = new User(uuid: UUID.randomUUID(), status: REGISTERED)
        def token = new Token(token: UUID.randomUUID(), user: user)
        def tokenService = Mock(TokenService)
        tokenServiceFactory.getTokenService(ACCOUNT_ACTIVATION_TOKEN) >> tokenService
        tokenService.getAndValidateToken(token.token) >> token

        when:
        def response = authService.activateAccount(UUID.fromString(token.token))

        then:
        1 * userRepository.save(user)
        1 * tokenRepository.save(token)
        user.status == ACTIVE
        token.usedAt != null
        response.status == S00000
    }

    def "activateAccount should throw exception if user already ACTIVE"() {
        given:
        def user = new User(uuid: UUID.randomUUID(), status: ACTIVE)
        def token = new Token(token: UUID.randomUUID(), user: user)
        def tokenService = Mock(TokenService)
        tokenServiceFactory.getTokenService(ACCOUNT_ACTIVATION_TOKEN) >> tokenService
        tokenService.getAndValidateToken(token.token) >> token

        when:
        authService.activateAccount(UUID.fromString(token.token))

        then:
        def ex = thrown(ServiceException)
        ex.getStatus() == E03003
    }

    def "authenticate should throw exception if user not active"() {
        given:
        def user = new User(uuid: UUID.randomUUID(), email: "test@example.com", status: REGISTERED)
        def request = new LoginRequest(user.email, "pass")
        userService.getUserByEmail(user.email) >> user

        when:
        authService.authenticate(request)

        then:
        def ex = thrown(ServiceException)
        ex.getStatus() == E03002
    }

    def "authenticate should generate tokens for active user"() {
        given:
        def user = new User(uuid: UUID.randomUUID(), email: "test@example.com", status: ACTIVE)
        def request = new LoginRequest(user.email, "pass")
        userService.getUserByEmail(user.email) >> user
        authenticationManager.authenticate(_ as Authentication) >> { return null }
        jwtService.revokeAllUserJwtTokens(user.uuid) >> null

        def accessToken = new Token(token: UUID.randomUUID().toString(), tokenType: "ACCESS_TOKEN")
        def refreshToken = new Token(token: UUID.randomUUID().toString(), tokenType: "REFRESH_TOKEN")
        jwtService.generateAccessToken(user.email) >> "access"
        jwtService.generateRefreshToken(user.email) >> "refresh"
        jwtService.buildJwtTokenEntity(user, "access", ACCESS_TOKEN) >> accessToken
        jwtService.buildJwtTokenEntity(user, "refresh", REFRESH_TOKEN) >> refreshToken

        when:
        def response = authService.authenticate(request)

        then:
        1 * tokenRepository.saveAll([accessToken, refreshToken])
        response.accessToken() == accessToken.getToken()
        response.refreshToken() == refreshToken.getToken()
    }

    def "refreshToken should generate new access token"() {
        given:
        def user = new User(uuid: UUID.randomUUID(), email: "test@example.com")
        def refreshTokenStr = "refreshToken"
        def request = Mock(HttpServletRequest)
        request.getHeader("Authorization") >> "Bearer ${refreshTokenStr}"
        jwtService.readTokenFromHeader(request) >> refreshTokenStr
        jwtService.extractUsernameFromToken(refreshTokenStr) >> user.email
        userService.getUserByEmail(user.email) >> user

        def accessToken = new Token(token: UUID.randomUUID(), tokenType: "ACCESS_TOKEN")
        jwtService.generateAccessToken(user.email) >> "access"
        jwtService.buildJwtTokenEntity(user, "access", ACCESS_TOKEN) >> accessToken

        when:
        def response = authService.refreshToken(request)

        then:
        1 * tokenRepository.save(accessToken)
        response.accessToken() == accessToken.getToken()
        response.refreshToken() == refreshTokenStr
    }
}