package pl.biketrack.file.service.impl

import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import pl.biketrack.exception.exception.CustomValidationException
import pl.biketrack.exception.exception.ServiceException
import pl.biketrack.properties.FileStorageProperties
import pl.biketrack.user.model.User
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

import static java.util.UUID.randomUUID
import static pl.biketrack.common.enumerated.ResponseCode.E00000
import static pl.biketrack.common.enumerated.ResponseCode.E07000
import static pl.biketrack.common.enumerated.ResponseCode.E07001
import static pl.biketrack.file.enumerated.FileDirectory.BIKES

class FileStorageServiceImplTest extends Specification {

    def props = Mock(FileStorageProperties)
    def service = new FileStorageServiceImpl(props)

    Path uploadDir
    UUID userUuid = randomUUID()
    def directory = BIKES

    def setup() {
        def user = new User(uuid: userUuid, email: "user@example.com", nickname: "nick", password: "encoded-pass")

        def authentication = new TestingAuthenticationToken(user, null)
        authentication.setAuthenticated(true)
        SecurityContextHolder.getContext().setAuthentication(authentication)

        uploadDir = Files.createTempDirectory("upload-test")
        props.getUploadPath() >> uploadDir
    }

    def cleanup() {
        SecurityContextHolder.clearContext()

        uploadDir?.toFile()?.deleteDir()
    }

    def "should save file successfully"() {
        given:
        def file = new MockMultipartFile("file", "test.png", "image/png", "data".bytes)

        when:
        def uuid = service.saveFile(file, userUuid, directory, "file")

        then:
        Files.exists(uploadDir.resolve(userUuid.toString()).resolve(directory.getPath()))
        uuid != null
    }

    def "should throw when file name invalid"() {
        given:
        def file = new MockMultipartFile("file", "", "image/png", "data".bytes)

        when:
        service.saveFile(file, userUuid, directory, "file")

        then:
        def ex = thrown(CustomValidationException)
        ex.status == E00000
        ex.errors[0].field() == "file"
        ex.errors[0].message() == "file must have a name"
    }

    def "should throw when error during file saving"() {
        given:
        def file = Mock(MockMultipartFile)
        file.getOriginalFilename() >> "file.png"
        file.getInputStream() >> { throw new IOException("boom") }

        when:
        service.saveFile(file, userUuid, directory, "file")

        then:
        def ex = thrown(ServiceException)
        ex.status == E07000
    }

    def "should serve file inline"() {
        given:
        def file = new MockMultipartFile("file", "photo.png", "image/png", "abc".bytes)
        def uuid = service.saveFile(file, userUuid, directory, "file")

        when:
        def response = service.serveFile(uuid, directory, true)

        then:
        response instanceof ResponseEntity<Resource>
        response.getHeaders().get("Content-Disposition")[0].startsWith("inline")
    }

    def "should throw when serving non-existent file"() {
        when:
        service.serveFile(randomUUID(), directory, false)

        then:
        def ex = thrown(ServiceException)
        ex.status == E07001
    }

    def "should delete existing file successfully"() {
        given:
        def file = new MockMultipartFile("file", "photo.png", "image/png", "abc".bytes)
        def uuid = service.saveFile(file, userUuid, directory, "file")

        expect:
        Files.exists(uploadDir.resolve(userUuid.toString()).resolve(directory.getPath()))

        when:
        service.deleteFile(uuid, directory)

        then:
        !Files.list(uploadDir.resolve(userUuid.toString()).resolve(directory.getPath())).any()
    }

    def "should throw when deleting missing directory"() {
        when:
        service.deleteFile(randomUUID(), directory)

        then:
        def ex = thrown(ServiceException)
        ex.status == E07001
    }

    def "should skip invalid deletes in deleteFiles()"() {
        given:
        def invalidUuid = randomUUID()
        and:
        service.metaClass.deleteFile = { uuid, dir -> throw new ServiceException(E07001) }

        when:
        service.deleteFiles([invalidUuid], directory)

        then:
        noExceptionThrown()
    }
}