package pl.biketrack.user.service.impl

import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import pl.biketrack.exception.exception.ServiceException
import pl.biketrack.user.dto.request.ChangePasswordRequest
import pl.biketrack.user.dto.request.UpdateUserRequest
import pl.biketrack.user.model.User
import pl.biketrack.user.repository.UserRepository
import spock.lang.Specification
import spock.lang.Subject

import static java.util.UUID.randomUUID
import static pl.biketrack.common.enumerated.ResponseCode.E03001
import static pl.biketrack.common.enumerated.ResponseCode.E03004
import static pl.biketrack.common.enumerated.ResponseCode.E03005
import static pl.biketrack.common.enumerated.ResponseCode.E03006
import static pl.biketrack.common.enumerated.ResponseCode.E03007
import static pl.biketrack.common.enumerated.ResponseCode.S00000

class UserServiceImplTest extends Specification {

    def userRepository = Mock(UserRepository)
    def passwordEncoder = Mock(PasswordEncoder)

    @Subject
    def userService = new UserServiceImpl(userRepository, passwordEncoder)

    def user = new User(uuid: randomUUID(), email: "user@example.com", nickname: "nick", password: "encoded-pass")

    def setup() {
        def authentication = new TestingAuthenticationToken(user, null)
        authentication.setAuthenticated(true)
        SecurityContextHolder.getContext().setAuthentication(authentication)
    }

    def cleanup() {
        SecurityContextHolder.clearContext()
    }

    def "getUserByEmail should return user when exists"() {
        given:
        def email = "user@example.com"
        userRepository.findByEmail(email) >> Optional.of(user)

        when:
        def result = userService.getUserByEmail(email)

        then:
        result == user
    }

    def "getUserByEmail should throw exception when user not found"() {
        given:
        def email = "notfound@example.com"
        userRepository.findByEmail(email) >> Optional.empty()

        when:
        userService.getUserByEmail(email)

        then:
        def ex = thrown(ServiceException)
        ex.status == E03001
    }

    def "getUserDetails should return details of logged user"() {
        when:
        def response = userService.getUserDetails()

        then:
        response.email() == user.email
        response.nickname() == user.nickname
    }

    def "updateUser should update user when data valid"() {
        given:
        def request = new UpdateUserRequest("newnick", "new@example.com")

        userRepository.isEmailTaken(request.email(), user.uuid) >> false
        userRepository.isNicknameTaken(request.nickname(), user.uuid) >> false

        when:
        def response = userService.updateUser(request)

        then:
        1 * userRepository.save(_ as User)
        response.status == S00000
        user.email == "new@example.com"
        user.nickname == "newnick"
    }

    def "updateUser should throw when email already taken"() {
        given:
        def request = new UpdateUserRequest("nick", "other@example.com")

        userRepository.isEmailTaken(request.email(), user.uuid) >> true

        when:
        userService.updateUser(request)

        then:
        def ex = thrown(ServiceException)
        ex.status == E03006
    }

    def "updateUser should throw when nickname already taken"() {
        given:
        def request = new UpdateUserRequest("othernick", "user@example.com")

        userRepository.isNicknameTaken(request.nickname(), user.uuid) >> true

        when:
        userService.updateUser(request)

        then:
        def ex = thrown(ServiceException)
        ex.status == E03007
    }

    def "changePassword should update password when valid"() {
        given:
        def request = new ChangePasswordRequest("newpass", "newpass")

        passwordEncoder.matches(request.password(), user.password) >> false
        passwordEncoder.encode(request.password()) >> "encoded-newpass"

        when:
        def response = userService.changePassword(request)

        then:
        1 * userRepository.save(_ as User)
        user.password == "encoded-newpass"
        response.status == S00000
    }

    def "changePassword should throw when passwords do not match"() {
        given:
        def request = new ChangePasswordRequest("pass1", "pass2")

        when:
        userService.changePassword(request)

        then:
        def ex = thrown(ServiceException)
        ex.status == E03004
    }

    def "changePassword should throw when same as current password"() {
        given:
        def request = new ChangePasswordRequest("samepass", "samepass")

        passwordEncoder.matches(request.password(), user.password) >> true

        when:
        userService.changePassword(request)

        then:
        def ex = thrown(ServiceException)
        ex.status == E03005
    }
}