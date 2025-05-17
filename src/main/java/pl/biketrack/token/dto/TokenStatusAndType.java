package pl.biketrack.token.dto;

import pl.biketrack.token.enumerated.TokenType;

public record TokenStatusAndType(boolean isRevoked, TokenType tokenType) {}