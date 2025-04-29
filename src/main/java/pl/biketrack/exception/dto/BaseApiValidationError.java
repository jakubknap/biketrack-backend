package pl.biketrack.exception.dto;

public record BaseApiValidationError(String field, String message) {}