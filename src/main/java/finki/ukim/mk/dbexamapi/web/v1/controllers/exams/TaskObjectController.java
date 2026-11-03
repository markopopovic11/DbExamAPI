package finki.ukim.mk.dbexamapi.web.v1.controllers.exams;

import finki.ukim.mk.dbexamapi.web.v1.mappers.exams.TaskObjectMapper;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.TaskObjectRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.TaskObjectResponse;
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
@RequestMapping("/v1/task-objects")
public class TaskObjectController {

    private final TaskObjectMapper taskObjectMapper;

    public TaskObjectController(TaskObjectMapper taskObjectMapper) {
        this.taskObjectMapper = taskObjectMapper;
    }

    @GetMapping
    public ResponseEntity<List<TaskObjectResponse>> findAll() {
        return ResponseEntity.ok(taskObjectMapper.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskObjectResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(taskObjectMapper.findById(id));
    }

    @PostMapping
    public ResponseEntity<TaskObjectResponse> create(@Valid @RequestBody TaskObjectRequest request) {
        return ResponseEntity.ok(taskObjectMapper.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskObjectResponse> update(@PathVariable String id,
                                                     @Valid @RequestBody TaskObjectRequest request) {
        return ResponseEntity.ok(taskObjectMapper.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TaskObjectResponse> deleteById(@PathVariable String id) {
        return ResponseEntity.ok(taskObjectMapper.deleteById(id));
    }
}
