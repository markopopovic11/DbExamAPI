package finki.ukim.mk.dbexamapi.web.v1.controllers.datasets;

import finki.ukim.mk.dbexamapi.web.v1.mappers.datasets.TaskVersionMapper;
import finki.ukim.mk.dbexamapi.web.v1.requests.datasets.TaskVersionRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.datasets.TaskVersionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/task-versions")
public class TaskVersionController {

    private final TaskVersionMapper taskVersionMapper;

    public TaskVersionController(TaskVersionMapper taskVersionMapper) {
        this.taskVersionMapper = taskVersionMapper;
    }

    @GetMapping
    public ResponseEntity<List<TaskVersionResponse>> findAll() {
        return ResponseEntity.ok(taskVersionMapper.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskVersionResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(taskVersionMapper.findById(id));
    }

    @PostMapping
    public ResponseEntity<TaskVersionResponse> create(@Valid @RequestBody TaskVersionRequest request) {
        return ResponseEntity.ok(taskVersionMapper.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskVersionResponse> update(@PathVariable String id,
                                                      @Valid @RequestBody TaskVersionRequest request) {
        return ResponseEntity.ok(taskVersionMapper.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TaskVersionResponse> deleteById(@PathVariable String id) {
        return ResponseEntity.ok(taskVersionMapper.deleteById(id));
    }
}
