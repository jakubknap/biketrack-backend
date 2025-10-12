package pl.biketrack.file.validator

import org.springframework.mock.web.MockMultipartFile
import pl.biketrack.exception.exception.CustomValidationException
import pl.biketrack.file.enumerated.FileType
import pl.biketrack.properties.FileStorageProperties
import spock.lang.Specification

import static pl.biketrack.common.enumerated.ResponseCode.E00000

class FileValidatorTest extends Specification {

    def properties = Mock(FileStorageProperties)
    def validator = new FileValidator(properties)
    def fileField = "file"

    def "should throw when file is null"() {
        when:
        validator.validate(null, fileField, FileType.IMAGE)

        then:
        def ex = thrown(CustomValidationException)
        ex.status == E00000
        ex.errors[0].field() == fileField
        ex.errors[0].message() == "must not be empty"
    }

    def "should throw when file is empty"() {
        given:
        def file = new MockMultipartFile("f", "img.jpg", "image/jpeg", new byte[0])

        when:
        validator.validate(file, fileField, FileType.IMAGE)

        then:
        def ex = thrown(CustomValidationException)
        ex.status == E00000
        ex.errors[0].field() == fileField
        ex.errors[0].message() == "must not be empty"
    }

    def "should throw when file type is invalid"() {
        given:
        def file = new MockMultipartFile("f", "img.jpg", "text/plain", "abc".bytes)

        when:
        validator.validate(file, fileField, FileType.IMAGE)

        then:
        def ex = thrown(CustomValidationException)
        ex.status == E00000
        ex.errors[0].field() == fileField
        ex.errors[0].message() == "must be a valid file type"
    }

    def "should throw when file too large"() {
        given:
        def file = new MockMultipartFile("f", "img.jpg", "image/jpeg", new byte[100])
        properties.getMaxFileSize() >> 50L

        when:
        validator.validate(file, fileField, FileType.IMAGE)

        then:
        def ex = thrown(CustomValidationException)
        ex.status == E00000
        ex.errors[0].field() == fileField
        ex.errors[0].message() == "file is too large"
    }

    def "should throw when extension not allowed"() {
        given:
        def file = new MockMultipartFile("f", "document.pdf", "image/jpeg", "abc".bytes)
        properties.getMaxFileSize() >> null

        when:
        validator.validate(file, fileField, FileType.IMAGE)

        then:
        def ex = thrown(CustomValidationException)
        ex.status == E00000
        ex.errors[0].field() == fileField
        ex.errors[0].message() == "not allowed file type"
    }

    def "should pass validation for valid image"() {
        given:
        def file = new MockMultipartFile("f", "photo.png", "image/png", "123".bytes)
        properties.getMaxFileSize() >> 1000L

        when:
        validator.validate(file, fileField, FileType.IMAGE)

        then:
        noExceptionThrown()
    }

    def "should throw when too many files"() {
        given:
        def file = new MockMultipartFile("f", "photo.png", "image/png", "123".bytes)
        properties.getMaxFilesPerRequest() >> 2

        when:
        validator.validateAll([file, file, file], fileField, FileType.IMAGE)

        then:
        def ex = thrown(CustomValidationException)
        ex.status == E00000
        ex.errors[0].field() == fileField
        ex.errors[0].message() == "too many files"
    }

    def "should throw when total size too large"() {
        given:
        def file1 = new MockMultipartFile("f1", "a.png", "image/png", new byte[10])
        def file2 = new MockMultipartFile("f2", "b.png", "image/png", new byte[20])
        properties.getMaxFilesPerRequest() >> 10
        properties.getMaxTotalSize() >> 25L
        properties.getMaxFileSize() >> null

        when:
        validator.validateAll([file1, file2], fileField, FileType.IMAGE)

        then:
        def ex = thrown(CustomValidationException)
        ex.status == E00000
        ex.errors[0].field() == fileField
        ex.errors[0].message() == "total files size is too large"
    }

    def "should pass validateAll when valid"() {
        given:
        def file1 = new MockMultipartFile("f1", "a.jpg", "image/jpeg", new byte[10])
        def file2 = new MockMultipartFile("f2", "b.png", "image/png", new byte[20])
        properties.getMaxFilesPerRequest() >> 5
        properties.getMaxTotalSize() >> 100L
        properties.getMaxFileSize() >> 1000L

        when:
        validator.validateAll([file1, file2], fileField, FileType.IMAGE)

        then:
        noExceptionThrown()
    }
}