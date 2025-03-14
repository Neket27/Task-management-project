package app.service;

import app.dto.SingleReceiverRequest;

public interface EmailService {

    void sendTextEmail(SingleReceiverRequest request);

}
