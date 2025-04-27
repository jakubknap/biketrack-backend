package pl.biketrack.base.controller.advice;

public record BaseApiValidationError(String field, String message) {}