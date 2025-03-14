package app.mapper.task;


import app.dto.CreateTaskDto;
import app.dto.TaskDto;
import app.dto.UpdateTaskDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import app.entity.Task;

@Mapper(componentModel = "spring")
public interface TaskMapper extends BaseMapper<Task, TaskDto> {

    Task toEntity(CreateTaskDto dto);

    Task toEntity(UpdateTaskDto dto);

    Task toEntity(Long id, UpdateTaskDto dto);

    void update(@MappingTarget Task task, Task taskFromDto);
}
