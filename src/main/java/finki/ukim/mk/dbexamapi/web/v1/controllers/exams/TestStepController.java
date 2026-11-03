package finki.ukim.mk.dbexamapi.web.v1.controllers.exams;

import finki.ukim.mk.dbexamapi.web.v1.mappers.exams.TestStepMapper;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.TestStepRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.TestStepResponse;
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
@RequestMapping("/v1/test-steps")
public class TestStepController {

    private final TestStepMapper testStepMapper;

    public TestStepController(TestStepMapper testStepMapper) {
        this.testStepMapper = testStepMapper;
    }

    @GetMapping
    public ResponseEntity<List<TestStepResponse>> findAll() {
        return ResponseEntity.ok(testStepMapper.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestStepResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(testStepMapper.findById(id));
    }

    @PostMapping
    public ResponseEntity<TestStepResponse> create(@Valid @RequestBody TestStepRequest request) {
        return ResponseEntity.ok(testStepMapper.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestStepResponse> update(@PathVariable String id,
                                                   @Valid @RequestBody TestStepRequest request) {
        return ResponseEntity.ok(testStepMapper.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TestStepResponse> deleteById(@PathVariable String id) {
        return ResponseEntity.ok(testStepMapper.deleteById(id));
    }
}
