package pl.biketrack.dashboard.service.impl

import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import pl.biketrack.bike.repository.BikeRepository
import pl.biketrack.dashboard.dto.MoneyDto
import pl.biketrack.dashboard.dto.RecentlyAddedBikeDto
import pl.biketrack.dashboard.dto.RecentlyAddedRepairDto
import pl.biketrack.repair.dto.RepairStatisticsDto
import pl.biketrack.repair.repository.RepairRepository
import pl.biketrack.user.model.User
import spock.lang.Specification

import java.time.LocalDateTime

import static com.neovisionaries.i18n.CurrencyCode.PLN
import static java.util.UUID.randomUUID

class DashboardServiceImplTest extends Specification {

    def bikeRepository = Mock(BikeRepository)
    def repairRepository = Mock(RepairRepository)
    def dashboardService = new DashboardServiceImpl(bikeRepository, repairRepository)

    def userUuid = randomUUID()

    def setup() {
        def user = new User(uuid: userUuid, email: "user@example.com", nickname: "nick", password: "encoded-pass")

        def authentication = new TestingAuthenticationToken(user, null)
        authentication.setAuthenticated(true)
        SecurityContextHolder.getContext().setAuthentication(authentication)
    }

    def "should return zero statistics when no bikes or repairs"() {
        given:
        bikeRepository.countByUserUuid(userUuid) >> 0
        repairRepository.getRepairStatisticsDtoForUser(userUuid) >> []

        when:
        def result = dashboardService.getDashboardStatistics()

        then:
        0 * bikeRepository.findRecentlyAddedBikeByUserUuid(userUuid)
        0 * repairRepository.findRecentlyAddedRepairByUserUuid(userUuid)
        result.totalBikes() == 0
        result.recentlyAddedBike() == null
        result.totalRepairs() == 0
        result.totalRepairsCost().amount() == 0
        result.totalRepairsCost().currency() == null
        result.recentlyAddedRepair() == null
    }

    def "should return statistics with one bike and one repair"() {
        given:
        def bike = new RecentlyAddedBikeDto("Bike1", randomUUID())
        def repair = new RecentlyAddedRepairDto(randomUUID(), "Repair1", new MoneyDto(new BigDecimal("120.50"), PLN))
        def repairStats = [new RepairStatisticsDto(new BigDecimal("120.50"), PLN, LocalDateTime.now())]

        bikeRepository.countByUserUuid(userUuid) >> 1
        bikeRepository.findRecentlyAddedBikeByUserUuid(userUuid) >> [bike]
        repairRepository.getRepairStatisticsDtoForUser(userUuid) >> repairStats
        repairRepository.findRecentlyAddedRepairByUserUuid(userUuid) >> [repair]

        when:
        def result = dashboardService.getDashboardStatistics()

        then:
        result.totalBikes() == 1
        result.recentlyAddedBike() == bike
        result.totalRepairs() == 1
        result.totalRepairsCost().amount() == new BigDecimal("120.50")
        result.totalRepairsCost().currency() == PLN
        result.recentlyAddedRepair() == repair
    }

    def "should correctly sum multiple repairs and get last repair currency"() {
        given:
        def repairStats = [new RepairStatisticsDto(new BigDecimal("50.00"), PLN, LocalDateTime.now().minusDays(2)),
                           new RepairStatisticsDto(new BigDecimal("100.00"), PLN, LocalDateTime.now())]
        bikeRepository.countByUserUuid(userUuid) >> 0
        repairRepository.getRepairStatisticsDtoForUser(userUuid) >> repairStats
        repairRepository.findRecentlyAddedRepairByUserUuid(userUuid) >> []

        when:
        def result = dashboardService.getDashboardStatistics()

        then:
        result.totalBikes() == 0
        result.recentlyAddedBike() == null
        result.totalRepairs() == 2
        result.totalRepairsCost().amount() == new BigDecimal("150.00")
        result.totalRepairsCost().currency() == PLN
        result.recentlyAddedRepair() == null
    }

    def "should handle null cost and currency in repairs"() {
        given:
        def repairStats = [new RepairStatisticsDto(null, null, LocalDateTime.now())]
        bikeRepository.countByUserUuid(userUuid) >> 0
        repairRepository.getRepairStatisticsDtoForUser(userUuid) >> repairStats
        repairRepository.findRecentlyAddedRepairByUserUuid(userUuid) >> []

        when:
        def result = dashboardService.getDashboardStatistics()

        then:
        result.totalBikes() == 0
        result.totalRepairs() == 1
        result.totalRepairsCost().amount() == 0
        result.totalRepairsCost().currency() == null
    }
}