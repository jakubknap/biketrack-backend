package pl.biketrack.bike.service.impl

import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import pl.biketrack.bike.dto.request.CreateBikeRequest
import pl.biketrack.bike.dto.request.UpdateBikeRequest
import pl.biketrack.bike.dto.response.BikeDetailsResponse
import pl.biketrack.bike.dto.response.BikeSelectListResponse
import pl.biketrack.bike.model.Bike
import pl.biketrack.bike.repository.BikeRepository
import pl.biketrack.converter.PdfConverter
import pl.biketrack.exception.exception.ServiceException
import pl.biketrack.file.service.FileStorageService
import pl.biketrack.file.validator.FileValidator
import pl.biketrack.repair.dto.RepairStatisticsDto
import pl.biketrack.repair.model.Repair
import pl.biketrack.repair.model.RepairPhoto
import pl.biketrack.repair.repository.RepairRepository
import pl.biketrack.user.model.User
import spock.lang.Specification

import java.time.LocalDateTime

import static com.neovisionaries.i18n.CurrencyCode.PLN
import static java.util.UUID.randomUUID
import static pl.biketrack.common.enumerated.ResponseCode.E05000
import static pl.biketrack.common.enumerated.ResponseCode.E05001
import static pl.biketrack.common.enumerated.ResponseCode.S00003
import static pl.biketrack.file.enumerated.FileDirectory.BIKES

class BikeServiceImplTest extends Specification {

    def bikeRepository = Mock(BikeRepository)
    def repairRepository = Mock(RepairRepository)
    def fileValidator = Mock(FileValidator)
    def fileStorageService = Mock(FileStorageService)
    def pdfConverter = Mock(PdfConverter)

    def bikeService = new BikeServiceImpl(bikeRepository, repairRepository, fileValidator, fileStorageService, pdfConverter)

    def userUuid = randomUUID()
    def bikeUuid = randomUUID()

    def setup() {
        def user = new User(uuid: userUuid, email: "user@example.com", nickname: "nick", password: "encoded-pass")

        def authentication = new TestingAuthenticationToken(user, null)
        authentication.setAuthenticated(true)
        SecurityContextHolder.getContext().setAuthentication(authentication)
    }

    def "getUserBikes should return list from repository"() {
        given:
        def list = [new BikeSelectListResponse(randomUUID(), "Bike1")]
        bikeRepository.getBikeList(userUuid) >> list

        expect:
        bikeService.getUserBikes() == list
    }

    def "createBike should save bike without photo"() {
        given:
        def request = new CreateBikeRequest("Bike1", null, null, "type", null, null, null, null)
        def file = null

        when:
        def resp = bikeService.createBike(request, file)

        then:
        1 * bikeRepository.save(_)
        resp.status == S00003
        resp != null
    }

    def "createBike should save bike with photo"() {
        given:
        def request = new CreateBikeRequest("Bike1", null, null, "type", null, null, null, null)
        def file = new MockMultipartFile("photo", "photo.png", "image/png", "data".bytes)
        fileStorageService.saveFile(file, userUuid, BIKES, "bikePhoto") >> randomUUID()

        when:
        def resp = bikeService.createBike(request, file)

        then:
        1 * fileValidator.validate(file, "bikePhoto", _)
        1 * fileStorageService.saveFile(file, userUuid, _, "bikePhoto")
        1 * bikeRepository.save(_)
        resp != null
    }

    def "getBike should throw if bike not found"() {
        given:
        bikeRepository.getBikeDetails(bikeUuid) >> Optional.empty()

        when:
        bikeService.getBike(bikeUuid)

        then:
        def ex = thrown(ServiceException)
        ex.getStatus() == E05000
    }

    def "getBike should throw if user not owner"() {
        given:
        def bike = new BikeDetailsResponse(bikeUuid, null, null, null, null, null, null, null, null, null, null, null, randomUUID())
        bikeRepository.getBikeDetails(bikeUuid) >> Optional.of(bike)

        when:
        bikeService.getBike(bikeUuid)

        then:
        def ex = thrown(ServiceException)
        ex.getStatus() == E05001
    }

    def "getBike should return bike if exists and user is owner"() {
        given:
        def bike = new BikeDetailsResponse(bikeUuid, null, null, null, null, null, null, null, null, null, null, null, userUuid)
        bikeRepository.getBikeDetails(bikeUuid) >> Optional.of(bike)

        expect:
        bikeService.getBike(bikeUuid) != null
    }

    def "getBikeRepairs should throw if bike not found"() {
        given:
        bikeRepository.findBikeWithUserByUuid(bikeUuid) >> Optional.empty()

        when:
        bikeService.getBikeRepairs(bikeUuid, Mock(Pageable))

        then:
        def ex = thrown(ServiceException)
        ex.getStatus() == E05000
    }

    def "getBikeRepairs should throw if user not owner"() {
        given:
        def bike = new Bike(user: new User(uuid: randomUUID()))
        bikeRepository.findBikeWithUserByUuid(bikeUuid) >> Optional.of(bike)

        when:
        bikeService.getBikeRepairs(bikeUuid, Mock(Pageable))

        then:
        def ex = thrown(ServiceException)
        ex.getStatus() == E05001
    }

    def "getBikeStatistics should throw if bike not found or user not owner"() {
        when:
        bikeService.getBikeStatistics(bikeUuid)

        then:
        bikeRepository.findBikeWithUserByUuid(bikeUuid) >> Optional.empty()

        def ex = thrown(ServiceException)
        ex.getStatus() == E05000
    }

    def "getBikeStatistics should return stats if bike exists"() {
        given:
        def bike = new Bike(user: new User(uuid: userUuid))
        def stats = [new RepairStatisticsDto(new BigDecimal("100.0"), PLN, LocalDateTime.now())]
        bikeRepository.findBikeWithUserByUuid(bikeUuid) >> Optional.of(bike)
        repairRepository.getRepairStatisticsDtoForBike(bikeUuid) >> stats

        when:
        def result = bikeService.getBikeStatistics(bikeUuid)

        then:
        result.totalRepairs() == 1
        result.totalRepairCost().amount() == new BigDecimal("100.0")
    }

    def "updateBike should throw if bike not found"() {
        given:
        def request = new UpdateBikeRequest(bikeUuid, "Updated", null, null, "type", null, null, null, null)
        bikeRepository.findBikeWithUserByUuid(bikeUuid) >> Optional.empty()

        when:
        bikeService.updateBike(request, null)

        then:
        def ex = thrown(ServiceException)
        ex.getStatus() == E05000
    }

    def "updateBike should update bike and handle photo"() {
        given:
        def bike = new Bike(uuid: bikeUuid, user: new User(uuid: userUuid))
        def request = new UpdateBikeRequest(bikeUuid, "Updated", null, null, "type", null, null, null, null)
        def photo = new MockMultipartFile("photo", "photo.png", "image/png", "data".bytes)
        bikeRepository.findBikeWithUserByUuid(bikeUuid) >> Optional.of(bike)
        fileStorageService.saveFile(photo, userUuid, BIKES, "bikePhoto") >> randomUUID()

        when:
        bikeService.updateBike(request, photo)

        then:
        1 * fileValidator.validate(photo, "bikePhoto", _)
        1 * fileStorageService.saveFile(photo, userUuid, _, "bikePhoto")
        1 * bikeRepository.save(bike)
    }

    def "deleteBike should delete bike and repair photos"() {
        given:
        def repairPhoto = new RepairPhoto(uuid: randomUUID())
        def repair = new Repair(photos: [repairPhoto])
        def bike = new Bike(uuid: bikeUuid, user: new User(uuid: userUuid), repairs: [repair], photoUuid: randomUUID())
        bikeRepository.findBikeWithUserAndRepairsByUuid(bikeUuid) >> Optional.of(bike)

        when:
        bikeService.deleteBike(bikeUuid)

        then:
        1 * fileStorageService.deleteFiles([repairPhoto.uuid], _)
        1 * fileStorageService.deleteFile(bike.photoUuid, _)
        1 * bikeRepository.delete(bike)
    }

    def "generateBikeReport should throw if bike not found or user not owner"() {
        given:
        bikeRepository.findBikeWithUserAndRepairsByUuid(bikeUuid) >> Optional.empty()

        when:
        bikeService.generateBikeReport(bikeUuid)

        then:
        def ex = thrown(ServiceException)
        ex.getStatus() == E05000
    }

    def "generateBikeReport should return PDF if bike exists"() {
        given:
        def bike = new Bike(uuid: bikeUuid, user: new User(uuid: userUuid), name: "Bike1", type: "Type1")
        def pdf = "PDFDATA".bytes
        bikeRepository.findBikeWithUserAndRepairsByUuid(bikeUuid) >> Optional.of(bike)
        pdfConverter.generatePdf("bike-report", _ as Map<String, Object>) >> pdf

        when:
        ResponseEntity<byte[]> resp = bikeService.generateBikeReport(bikeUuid)

        then:
        resp.body == pdf
        resp.headers.getFirst("Content-Disposition").contains("Bike1")
    }
}