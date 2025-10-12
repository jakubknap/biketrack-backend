package pl.biketrack.authentication.service.impl

import org.springframework.security.crypto.password.PasswordEncoder
import pl.biketrack.authentication.dto.request.ConfirmResetPasswordRequest
import pl.biketrack.authentication.dto.request.ResetPasswordRequest
import pl.biketrack.exception.dto.response.BaseResponse
import pl.biketrack.exception.exception.ServiceException
import pl.biketrack.mail.service.MailService
import pl.biketrack.properties.FrontendProperties
import pl.biketrack.token.model.Token
import pl.biketrack.token.repository.TokenRepository
import pl.biketrack.token.service.TokenService
import pl.biketrack.token.service.TokenServiceFactory
import pl.biketrack.user.model.User
import pl.biketrack.user.repository.UserRepository
import pl.biketrack.user.service.UserService
import spock.lang.Specification

import static pl.biketrack.common.enumerated.ResponseCode.E03001
import static pl.biketrack.common.enumerated.ResponseCode.E03004
import static pl.biketrack.common.enumerated.ResponseCode.E03005
import static pl.biketrack.token.enumerated.TokenType.PASSWORD_RESET_TOKEN

class PasswordServiceImplTest extends Specification {

    def userService = Mock(UserService)
    def tokenServiceFactory = Mock(TokenServiceFactory)
    def mailService = Mock(MailService)
    def frontendProperties = Mock(FrontendProperties)
    def tokenRepository = Mock(TokenRepository)
    def passwordEncoder = Mock(PasswordEncoder)
    def userRepository = Mock(UserRepository)

    def passwordService = new PasswordServiceImpl(userService, tokenServiceFactory, mailService, frontendProperties, tokenRepository, passwordEncoder, userRepository)

    def "resetPasswordRequest should send email if user exists"() {
        given:
        def email = "test@example.com"
        def user = new User(uuid: UUID.randomUUID(), email: email, nickname: "nick", password: "encoded")
        def tokenService = Mock(TokenService)

        userService.getUserByEmail(email) >> user
        tokenServiceFactory.getTokenService(PASSWORD_RESET_TOKEN) >> tokenService
        tokenService.generateToken(user) >> "token123"
        frontendProperties.prepareResetPasswordLink("token123") >> "https://link"

        when:
        def response = passwordService.resetPasswordRequest(new ResetPasswordRequest(email))

        then:
        1 * tokenService.revokeAllUserTokensByType(user.uuid)
        1 * mailService.sendMail(_)
        response instanceof BaseResponse
    }

    def "resetPasswordRequest should return generic response if user not found"() {
        given:
        def email = "notfound@example.com"
        userService.getUserByEmail(email) >> { throw new ServiceException(E03001) }

        when:
        def response = passwordService.resetPasswordRequest(new ResetPasswordRequest(email))

        then:
        response instanceof BaseResponse
        0 * mailService.sendMail(_)
    }

    def "confirmResetPassword should update password if valid"() {
        given:
        def user = new User(uuid: UUID.randomUUID(), password: "encodedOld")
        def token = new Token(token: UUID.randomUUID(), user: user)
        def tokenService = Mock(TokenService)
        def request = new ConfirmResetPasswordRequest(UUID.fromString(token.token), "newPass", "newPass")

        tokenServiceFactory.getTokenService(PASSWORD_RESET_TOKEN) >> tokenService
        tokenService.getAndValidateToken(token.token.toString()) >> token
        passwordEncoder.matches("newPass", "encodedOld") >> false
        passwordEncoder.encode("newPass") >> "encodedNew"

        when:
        def response = passwordService.confirmResetPassword(request)

        then:
        1 * userRepository.save(user)
        1 * tokenRepository.save(token)
        response instanceof BaseResponse
        user.password == "encodedNew"
        token.usedAt != null
    }

    def "confirmResetPassword should throw exception if passwords do not match"() {
        given:
        def user = new User(uuid: UUID.randomUUID(), password: "encodedOld")
        def token = new Token(token: UUID.randomUUID(), user: user)
        def tokenService = Mock(TokenService)
        def request = new ConfirmResetPasswordRequest(UUID.fromString(token.token), "pass1", "pass2")

        tokenServiceFactory.getTokenService(PASSWORD_RESET_TOKEN) >> tokenService
        tokenService.getAndValidateToken(token.token.toString()) >> token

        when:
        passwordService.confirmResetPassword(request)

        then:
        def ex = thrown(ServiceException)
        ex.getStatus() == E03004
        0 * userRepository.save(_)
    }

    def "confirmResetPassword should throw exception if new password is same as old"() {
        given:
        def user = new User(uuid: UUID.randomUUID(), password: "encodedOld")
        def token = new Token(token: UUID.randomUUID(), user: user)
        def tokenService = Mock(TokenService)
        def request = new ConfirmResetPasswordRequest(UUID.fromString(token.token), "samePass", "samePass")

        tokenServiceFactory.getTokenService(PASSWORD_RESET_TOKEN) >> tokenService
        tokenService.getAndValidateToken(token.token.toString()) >> token
        passwordEncoder.matches("samePass", "encodedOld") >> true

        when:
        passwordService.confirmResetPassword(request)

        then:
        def ex = thrown(ServiceException)
        ex.getStatus() == E03005
        0 * userRepository.save(_)
    }
}