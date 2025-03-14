package app.service.Impl;


import app.controller.advice.annotation.CustomExceptionHandler;
import app.dto.CreateTaskDto;
import app.dto.TaskDto;
import app.dto.UpdateTaskDto;
import app.entity.Status;
import app.entity.Task;
import app.exception.NotFoundException;
import app.mapper.task.TaskMapper;
import app.repository.TaskRepository;
import app.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@CustomExceptionHandler
public class TaskServiceImpl implements TaskService {

    @Value("${task.limit-downloads}")
    private Integer limit;

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
