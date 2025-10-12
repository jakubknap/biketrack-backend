package pl.biketrack.repair.service.impl


import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import pl.biketrack.bike.model.Bike
import pl.biketrack.bike.service.BikeService
import pl.biketrack.exception.exception.ServiceException
import pl.biketrack.file.service.FileStorageService
import pl.biketrack.file.validator.FileValidator
import pl.biketrack.repair.dto.request.AddRepairRequest
import pl.biketrack.repair.dto.request.UpdateRepairRequest
import pl.biketrack.repair.model.Repair
import pl.biketrack.repair.model.RepairPhoto
import pl.biketrack.repair.repository.RepairRepository
import pl.biketrack.user.model.User
import spock.lang.Specification

import java.time.LocalDate

import static com.neovisionaries.i18n.CurrencyCode.PLN
import static java.util.UUID.randomUUID
import static pl.biketrack.common.enumerated.ResponseCode.E06000
import static pl.biketrack.common.enumerated.ResponseCode.E06001
import static pl.biketrack.file.enumerated.FileDirectory.REPAIRS
import static pl.biketrack.file.enumerated.FileType.IMAGE

class RepairServiceImplTest extends Specification {

    def repairRepository = Mock(RepairRepository)
    def bikeService = Mock(BikeService)
    def fileStorageService = Mock(FileStorageService)
    def fileValidator = Mock(FileValidator)
    def repairService = new RepairServiceImpl(repairRepository, bikeService, fileStorageService, fileValidator)

    def userUuid = randomUUID()

    def setup() {
        def user = new User(uuid: userUuid, email: "user@example.com", nickname: "nick", password: "encoded-pass")

        def authentication = new TestingAuthenticationToken(user, null)
        authentication.setAuthenticated(true)
        SecurityContextHolder.getContext().setAuthentication(authentication)
    }

    def "addRepair should save repair with photos"() {
        given:
        def bikeUuid = randomUUID()
        def bike = new Bike(uuid: bikeUuid, user: new User(uuid: userUuid))
        def photoFile = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".bytes)
        def request = new AddRepairRequest(bikeUuid, "title", "desc", new BigDecimal("100"), PLN, LocalDate.now())

        bikeService.findBikeWithUserOrElseThrow(bikeUuid) >> bike

        when:
        def response = repairService.addRepair(request, [photoFile])

        then:
        1 * fileValidator.validateAll([photoFile], "repairPhotos", IMAGE)
        1 * fileStorageService.saveFile(photoFile, userUuid, REPAIRS, "repairPhotos") >> randomUUID()
        1 * repairRepository.save(_)
        response != null
    }

    def "getRepair should return RepairDetailsResponse"() {
        given:
        def repairUuid = randomUUID()
        def bike = new Bike(uuid: randomUUID(), name: "Bike1")
        def repair = new Repair(uuid: repairUuid, bike: bike, user: new User(uuid: userUuid), title: "title", description: "desc", cost: new BigDecimal("50"), currency: PLN, repairDate: LocalDate.now(), photos: [])
        repairRepository.findRepairWithUserAndBikeByUuid(repairUuid) >> Optional.of(repair)

        when:
        def result = repairService.getRepair(repairUuid)

        then:
        result.repairUuid() == repairUuid
        result.bike().uuid() == bike.uuid
        result.photos().isEmpty()
    }

    def "updateRepair should update repair and photos"() {
        given:
        def repairUuid = randomUUID()
        def repair = new Repair(uuid: repairUuid, user: new User(uuid: userUuid), photos: [])
        def photoFile = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".bytes)
        def request = new UpdateRepairRequest(repairUuid, "title", "desc", new BigDecimal("100"), PLN, LocalDate.now())

        repairRepository.findRepairWithUserByUuid(repairUuid) >> Optional.of(repair)

        when:
        repairService.updateRepair(request, [photoFile])

        then:
        1 * fileValidator.validateAll([photoFile], "repairPhotos", IMAGE)
        1 * fileStorageService.saveFile(photoFile, userUuid, REPAIRS, "repairPhotos") >> randomUUID()
        1 * repairRepository.save(repair)
    }

    def "deleteRepair should delete repair and photos"() {
        given:
        def repairUuid = randomUUID()
        def photoUuid = randomUUID()
        def photo = new RepairPhoto(uuid: photoUuid)
        def repair = new Repair(uuid: repairUuid, user: new User(uuid: userUuid), photos: [photo])

        repairRepository.findRepairWithUserByUuid(repairUuid) >> Optional.of(repair)

        when:
        repairService.deleteRepair(repairUuid)

        then:
        1 * fileStorageService.deleteFiles([photoUuid], REPAIRS)
        1 * repairRepository.delete(repair)
        repair.photos.isEmpty()
    }

    def "getRepairPhotos should return photo UUIDs"() {
        given:
        def repairUuid = randomUUID()
        def photoUuid = randomUUID()
        def repair = new Repair(uuid: repairUuid, user: new User(uuid: userUuid), photos: [new RepairPhoto(uuid: photoUuid)])
        repairRepository.findRepairWithUserByUuid(repairUuid) >> Optional.of(repair)

        when:
        def photos = repairService.getRepairPhotos(repairUuid)

        then:
        photos == [photoUuid]
    }

    def "should throw ServiceException if repair not found"() {
        given:
        def repairUuid = randomUUID()
        repairRepository.findRepairWithUserByUuid(repairUuid) >> Optional.empty()

        when:
        repairService.getRepairPhotos(repairUuid)

        then:
        def ex = thrown(ServiceException)
        ex.status == E06000
    }

    def "should throw ServiceException if logged user is not owner"() {
        given:
        def repairUuid = randomUUID()
        def repair = new Repair(uuid: repairUuid, user: new User(uuid: randomUUID()), photos: [])
        repairRepository.findRepairWithUserByUuid(repairUuid) >> Optional.of(repair)

        when:
        repairService.getRepairPhotos(repairUuid)

        then:
        def ex = thrown(ServiceException)
        ex.status == E06001
    }
}