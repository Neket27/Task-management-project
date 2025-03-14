package app.handler;

import app.aspect.annotation.CustomLogging;
import app.dto.SingleReceiverRequest;
import app.event.TaskUpdatedStatusEvent;
import app.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@CustomLogging
public class EventHandlerTask {

    private final EmailService emailService;

    @KafkaListener(topics = "${spring.kafka.consumer.topics[0].name}")
    public void handle(TaskUpdatedStatusEvent taskUpdatedStatusEvent) {
        log.info("Updated status task: {}", taskUpdatedStatusEvent);
        String message = String.format("Статус задачи с ID %s изменился на %s", taskUpdatedStatusEvent.taskId(), taskUpdatedStatusEvent.status());
        emailService.sendTextEmail(new SingleReceiverRequest("dima27125@yandex.ru", "Изменение статуса задачи",message));
        log.info("An email has been sent regarding the updated status of the task, status task with id {}: {}.", taskUpdatedStatusEvent.taskId(), taskUpdatedStatusEvent.status());
    }
}
