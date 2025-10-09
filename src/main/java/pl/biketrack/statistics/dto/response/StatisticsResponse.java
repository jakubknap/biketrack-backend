package pl.biketrack.statistics.dto.response;

import pl.biketrack.dashboard.dto.MoneyDto;

import java.util.List;

public record StatisticsResponse(Summary summary,
                                 List<RepairsPerBike> repairsPerBike,
                                 List<AverageRepairCostPerBike> averageRepairCostPerBike,
                                 List<RepairsThisYearPerMonth> repairsThisYearPerMonth) {

    public record Summary(long totalBikes, long totalRepairs, MoneyDto totalRepairCost) {}

    public record RepairsPerBike(String bikeName, long repairs) implements RepairsPerBikeProjection {

        @Override
        public String getBikeName() {
            return bikeName;
        }

        @Override
        public Long getRepairs() {
            return repairs;
        }
    }

    public record AverageRepairCostPerBike(String bikeName, Double averageCost) implements AverageRepairCostPerBikeProjection {

        @Override
        public String getBikeName() {
            return bikeName;
        }

        @Override
        public Double getAverageCost() {
            return averageCost;
        }
    }

    public record RepairsThisYearPerMonth(String month, long repairs) {}

    public interface RepairsPerBikeProjection {

        String getBikeName();

        Long getRepairs();
    }

    public interface AverageRepairCostPerBikeProjection {

        String getBikeName();

        Double getAverageCost();
    }
}