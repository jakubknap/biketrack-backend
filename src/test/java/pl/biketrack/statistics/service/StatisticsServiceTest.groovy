package pl.biketrack.statistics.service


import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import pl.biketrack.bike.repository.BikeRepository
import pl.biketrack.repair.dto.RepairStatisticsDto
import pl.biketrack.repair.repository.RepairRepository
import pl.biketrack.statistics.dto.RepairsPerMonthDtoProjection
import pl.biketrack.statistics.dto.response.StatisticsResponse
import pl.biketrack.user.model.User
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.Year

import static com.neovisionaries.i18n.CurrencyCode.PLN
import static java.util.UUID.randomUUID

class StatisticsServiceTest extends Specification {

    def bikeRepository = Mock(BikeRepository)
    def repairRepository = Mock(RepairRepository)

    def statisticsService = new StatisticsService(bikeRepository, repairRepository)

    def userUuid = randomUUID()

    def setup() {
        def user = new User(uuid: userUuid, email: "user@example.com", nickname: "nick", password: "encoded-pass")

        def authentication = new TestingAuthenticationToken(user, null)
        authentication.setAuthenticated(true)
        SecurityContextHolder.getContext().setAuthentication(authentication)
    }

    def "should build statistics correctly with full data"() {
        given:
        def repairStats = [new RepairStatisticsDto(new BigDecimal("100.00"), PLN, LocalDateTime.now()),
                           new RepairStatisticsDto(new BigDecimal("50.00"), PLN, LocalDateTime.now()),
                           new RepairStatisticsDto(new BigDecimal("75.00"), PLN, LocalDateTime.now()),
                           new RepairStatisticsDto(new BigDecimal("29.99"), PLN, LocalDateTime.now())]
        def repairsPerBike = [[getBikeName: { "Bike1" }, getRepairs: { 2L }] as StatisticsResponse.RepairsPerBikeProjection,
                              [getBikeName: { "Bike2" }, getRepairs: { 1L }] as StatisticsResponse.RepairsPerBikeProjection]
        def avgCostPerBike = [[getBikeName: { "Bike1" }, getAverageCost: { 75.0d }] as StatisticsResponse.AverageRepairCostPerBikeProjection]
        def repairsPerMonth = [[getMonthNumber: { 1 }, getRepairsCount: { 5L }] as RepairsPerMonthDtoProjection,
                               [getMonthNumber: { 2 }, getRepairsCount: { 2L }] as RepairsPerMonthDtoProjection]

        bikeRepository.countByUserUuid(userUuid) >> 3L
        repairRepository.getRepairStatisticsDtoForUser(userUuid) >> repairStats
        repairRepository.findRepairsPerBikeByUserUuid(userUuid) >> repairsPerBike
        repairRepository.findAverageRepairCostPerBikeByUserUuid(userUuid) >> avgCostPerBike
        repairRepository.countRepairsPerMonthForYearByUserUuidAndYear(userUuid, Year.now().value) >> repairsPerMonth

        when:
        def result = statisticsService.getStatistics()

        then:
        result.summary.totalBikes == 3
        result.summary.totalRepairs == 4
        result.summary.totalRepairCost.amount == new BigDecimal("254.99")
        result.summary.totalRepairCost.currency == PLN

        result.repairsPerBike*.bikeName == ["Bike1", "Bike2"]
        result.repairsPerBike*.repairs == [2L, 1L]
        result.averageRepairCostPerBike.first().averageCost == 75.0

        result.repairsThisYearPerMonth.size() == 12
        result.repairsThisYearPerMonth[0].month == "Styczeń"
        result.repairsThisYearPerMonth[1].month == "Luty"
        result.repairsThisYearPerMonth[11].month == "Grudzień"
        result.repairsThisYearPerMonth[0].repairs == 0
        result.repairsThisYearPerMonth[1].repairs == 5
        result.repairsThisYearPerMonth[2].repairs == 2
        result.repairsThisYearPerMonth[11].repairs == 0
    }

    def "should handle empty repositories gracefully"() {
        given:
        bikeRepository.countByUserUuid(userUuid) >> 0L
        repairRepository.getRepairStatisticsDtoForUser(userUuid) >> []
        repairRepository.findRepairsPerBikeByUserUuid(userUuid) >> []
        repairRepository.findAverageRepairCostPerBikeByUserUuid(userUuid) >> []
        repairRepository.countRepairsPerMonthForYearByUserUuidAndYear(userUuid, Year.now().value) >> []

        when:
        def result = statisticsService.getStatistics()

        then:
        result.summary.totalBikes == 0
        result.summary.totalRepairs == 0
        result.summary.totalRepairCost.amount == 0
        result.repairsPerBike.empty
        result.averageRepairCostPerBike.empty
        result.repairsThisYearPerMonth.size() == 12
        result.repairsThisYearPerMonth.every { it.repairs == 0 }
    }

    def "should default repairs cost currency to PLN when none found"() {
        given:
        def repairStats = [new RepairStatisticsDto(new BigDecimal("100.00"), null, LocalDateTime.now())]

        bikeRepository.countByUserUuid(userUuid) >> 1L
        repairRepository.getRepairStatisticsDtoForUser(userUuid) >> repairStats
        repairRepository.findRepairsPerBikeByUserUuid(userUuid) >> []
        repairRepository.findAverageRepairCostPerBikeByUserUuid(userUuid) >> []
        repairRepository.countRepairsPerMonthForYearByUserUuidAndYear(userUuid, Year.now().value) >> []

        when:
        def result = statisticsService.getStatistics()

        then:
        result.summary.totalRepairCost.currency == null
    }

    def "should build months even if month numbers are out of range"() {
        given:
        def repairsPerMonth = [[getMonthNumber: { 0 }, getRepairsCount: { 2L }] as RepairsPerMonthDtoProjection,
                               [getMonthNumber: { 13 }, getRepairsCount: { 10L }] as RepairsPerMonthDtoProjection]

        bikeRepository.countByUserUuid(userUuid) >> 0L
        repairRepository.getRepairStatisticsDtoForUser(userUuid) >> []
        repairRepository.findRepairsPerBikeByUserUuid(userUuid) >> []
        repairRepository.findAverageRepairCostPerBikeByUserUuid(userUuid) >> []
        repairRepository.countRepairsPerMonthForYearByUserUuidAndYear(userUuid, Year.now().value) >> repairsPerMonth

        when:
        def result = statisticsService.getStatistics()

        then:
        result.repairsThisYearPerMonth.size() == 12
        result.repairsThisYearPerMonth.every { it.repairs >= 0 }
    }
}