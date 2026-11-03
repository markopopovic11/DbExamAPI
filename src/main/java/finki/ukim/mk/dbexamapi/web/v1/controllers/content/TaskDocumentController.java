package finki.ukim.mk.dbexamapi.web.v1.controllers.content;

import finki.ukim.mk.dbexamapi.web.v1.mappers.content.TaskDocumentMapper;
import finki.ukim.mk.dbexamapi.web.v1.requests.content.TaskDocumentRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.content.TaskDocumentResponse;
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
@RequestMapping("/v1/task-documents")
public class TaskDocumentController {

    private final TaskDocumentMapper taskDocumentMapper;

    public TaskDocumentController(TaskDocumentMapper taskDocumentMapper) {
        this.taskDocumentMapper = taskDocumentMapper;
    }

    @GetMapping
    public ResponseEntity<List<TaskDocumentResponse>> findAll() {
        return ResponseEntity.ok(taskDocumentMapper.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDocumentResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(taskDocumentMapper.findById(id));
    }

    @PostMapping
    public ResponseEntity<TaskDocumentResponse> create(@Valid @RequestBody TaskDocumentRequest request) {
        return ResponseEntity.ok(taskDocumentMapper.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDocumentResponse> update(@PathVariable String id,
                                                       @Valid @RequestBody TaskDocumentRequest request) {
        return ResponseEntity.ok(taskDocumentMapper.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TaskDocumentResponse> deleteById(@PathVariable String id) {
        return ResponseEntity.ok(taskDocumentMapper.deleteById(id));
    }
}
