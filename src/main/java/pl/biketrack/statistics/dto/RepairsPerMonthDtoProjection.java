package pl.biketrack.statistics.dto;

public interface RepairsPerMonthDtoProjection {

    Integer getMonthNumber();

    Long getRepairsCount();
}