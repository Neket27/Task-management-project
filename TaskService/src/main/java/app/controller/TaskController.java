package app.controller;


import app.controller.advice.annotation.CustomExceptionHandler;
import app.dto.CreateTaskDto;
import app.dto.TaskDto;
import app.dto.UpdateTaskDto;
import app.event.TaskUpdatedStatusEvent;
import app.kafka.KafkaClientProducer;
import app.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@CustomExceptionHandler
public class TaskController {

    private final TaskService taskService;
    private final KafkaClientProducer<TaskUpdatedStatusEvent> kafkaClientProducer;
    @Value("${spring.kafka.producer.topics[0].name}")
    private String topic;

    @PostMapping()
    public TaskDto create(@RequestBody CreateTaskDto dto) {
        return taskService.create(dto);
    }

    @GetMapping("/{id}")
    public TaskDto get(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @PutMapping("/{id}")
    public TaskDto update(@PathVariable Long id, @RequestBody UpdateTaskDto dto) {
        TaskDto taskDto = taskService.update(id, dto);
        kafkaClientProducer.sendTo(topic, new TaskUpdatedStatusEvent(taskDto.id(), taskDto.status()));
        return taskDto;
    }

    @DeleteMapping("/{id}")
    public HttpStatus delete(@PathVariable Long id) {
        taskService.remove(id);
        return HttpStatus.OK;
    }

    @GetMapping()
    public List<TaskDto> list(@RequestParam(required = false, defaultValue = "0") int page,
                              @RequestParam(required = false, defaultValue = "100") int size) {
        return taskService.getList(PageRequest.of(page,size));
    }

}
