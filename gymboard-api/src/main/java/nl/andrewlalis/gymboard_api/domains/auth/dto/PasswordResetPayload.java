package nl.andrewlalis.gymboard_api.domains.auth.dto;

public record PasswordResetPayload(String code, String newPassword) {}
