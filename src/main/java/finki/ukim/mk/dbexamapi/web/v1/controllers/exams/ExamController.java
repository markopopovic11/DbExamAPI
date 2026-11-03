package finki.ukim.mk.dbexamapi.web.v1.controllers.exams;

import finki.ukim.mk.dbexamapi.web.v1.mappers.exams.ExamMapper;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.ExamRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.ExamResponse;
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
@RequestMapping("/v1/exams")
public class ExamController {

    private final ExamMapper examMapper;

    public ExamController(ExamMapper examMapper) {
        this.examMapper = examMapper;
    }

    @GetMapping
    public ResponseEntity<List<ExamResponse>> findAll() {
        return ResponseEntity.ok(examMapper.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(examMapper.findById(id));
    }

    @PostMapping
    public ResponseEntity<ExamResponse> create(@Valid @RequestBody ExamRequest request) {
        return ResponseEntity.ok(examMapper.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamResponse> update(@PathVariable String id,
                                               @Valid @RequestBody ExamRequest request) {
        return ResponseEntity.ok(examMapper.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ExamResponse> deleteById(@PathVariable String id) {
        return ResponseEntity.ok(examMapper.deleteById(id));
    }
}
