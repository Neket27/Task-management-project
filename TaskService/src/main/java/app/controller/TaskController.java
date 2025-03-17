package app.controller;

import app.controller.advice.annotation.CustomExceptionHandler;
import app.dto.CreateTaskDto;
import app.dto.TaskDto;
import app.dto.UpdateTaskDto;
import app.service.TaskService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto create(@RequestBody CreateTaskDto dto) {
        return taskService.create(dto);
    }

    @GetMapping("/{id}")
    public TaskDto get(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDto update(@PathVariable Long id, @RequestBody UpdateTaskDto dto) {
        return taskService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.remove(id);
    }

    @GetMapping()
    public List<TaskDto> list(@RequestParam(required = false, defaultValue = "0") int page,
                              @RequestParam(required = false, defaultValue = "100") int size) {
        return taskService.getList(PageRequest.of(page, size));
    }

}
