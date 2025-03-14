package app.dto;

public record SingleReceiverRequest(
        String receiver,
        String subject,
        String text
) {}
