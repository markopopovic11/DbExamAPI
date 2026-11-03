package finki.ukim.mk.dbexamapi.web.v1.controllers.exams;

import finki.ukim.mk.dbexamapi.web.v1.mappers.exams.ExamGroupMapper;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.ExamGroupRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.ExamGroupResponse;
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
@RequestMapping("/v1/exam-groups")
public class ExamGroupController {

    private final ExamGroupMapper examGroupMapper;

    public ExamGroupController(ExamGroupMapper examGroupMapper) {
        this.examGroupMapper = examGroupMapper;
    }

    @GetMapping
    public ResponseEntity<List<ExamGroupResponse>> findAll() {
        return ResponseEntity.ok(examGroupMapper.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamGroupResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(examGroupMapper.findById(id));
    }

    @PostMapping
    public ResponseEntity<ExamGroupResponse> create(@Valid @RequestBody ExamGroupRequest request) {
        return ResponseEntity.ok(examGroupMapper.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamGroupResponse> update(@PathVariable String id,
                                                    @Valid @RequestBody ExamGroupRequest request) {
        return ResponseEntity.ok(examGroupMapper.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ExamGroupResponse> deleteById(@PathVariable String id) {
        return ResponseEntity.ok(examGroupMapper.deleteById(id));
    }
}
