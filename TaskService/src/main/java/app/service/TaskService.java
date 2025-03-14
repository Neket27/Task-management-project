package app.service;

import app.dto.CreateTaskDto;
import app.dto.TaskDto;
import app.dto.UpdateTaskDto;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface TaskService {

    TaskDto create(CreateTaskDto dto);

    TaskDto getById(Long id);

    TaskDto update(Long id, UpdateTaskDto dto);

    void remove(Long id);

    List<TaskDto> getList(PageRequest pageRequest);
}
