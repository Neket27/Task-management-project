package app.service.Impl;


import app.dto.CreateTaskDto;
import app.dto.TaskDto;
import app.dto.UpdateTaskDto;
import app.entity.Status;
import app.entity.Task;
import app.event.TaskUpdatedStatusEvent;
import app.exception.NotFoundException;
import app.kafka.KafkaClientProducer;
import app.mapper.task.TaskMapper;
import app.repository.TaskRepository;
import app.service.TaskService;
import custom.logger.annotation.CustomLogging;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@CustomLogging
public class TaskServiceImpl implements TaskService {

    @Value("${spring.kafka.producer.topics[0].name}")
    private String topic;

    private final KafkaClientProducer<TaskUpdatedStatusEvent> kafkaClientProducer;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskDto create(CreateTaskDto dto) {
        Task task = taskMapper.toEntity(dto);
        task.setStatus(Status.Active);
        task = taskRepository.save(task);
        return taskMapper.toDto(task);

    }

    @Override
    @Transactional
    public TaskDto getById(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException(Task.class, id));
        return taskMapper.toDto(task);
    }

    @Override
    @Transactional
    public TaskDto update(Long id, UpdateTaskDto dto) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException(Task.class, id));
        Task taskFromDto = taskMapper.toEntity(id, dto);

        taskMapper.update(task, taskFromDto);
        taskRepository.save(task);

        kafkaClientProducer.sendTo(topic, new TaskUpdatedStatusEvent(task.getId(), task.getStatus()));

        return taskMapper.toDto(task);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        taskRepository.findById(id).ifPresentOrElse(
                taskRepository::delete,
                () -> {
                    throw new NotFoundException(Task.class, id);
                }
        );
    }

    @Override
    @Transactional
    public List<TaskDto> getList(PageRequest pageRequest) {
        List<Task> tasks = taskRepository.findAll(pageRequest).toList();
        return taskMapper.toDto(tasks);
    }


}
