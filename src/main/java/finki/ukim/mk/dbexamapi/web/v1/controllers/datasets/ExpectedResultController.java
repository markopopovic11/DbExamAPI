package finki.ukim.mk.dbexamapi.web.v1.controllers.datasets;

import finki.ukim.mk.dbexamapi.web.v1.mappers.datasets.ExpectedResultMapper;
import finki.ukim.mk.dbexamapi.web.v1.requests.datasets.ExpectedResultRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.datasets.ExpectedResultResponse;
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
@RequestMapping("/v1/expected-results")
public class ExpectedResultController {

    private final ExpectedResultMapper expectedResultMapper;

    public ExpectedResultController(ExpectedResultMapper expectedResultMapper) {
        this.expectedResultMapper = expectedResultMapper;
    }

    @GetMapping
    public ResponseEntity<List<ExpectedResultResponse>> findAll() {
        return ResponseEntity.ok(expectedResultMapper.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpectedResultResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(expectedResultMapper.findById(id));
    }

    @PostMapping
    public ResponseEntity<ExpectedResultResponse> create(@Valid @RequestBody ExpectedResultRequest request) {
        return ResponseEntity.ok(expectedResultMapper.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpectedResultResponse> update(@PathVariable String id,
                                                         @Valid @RequestBody ExpectedResultRequest request) {
        return ResponseEntity.ok(expectedResultMapper.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ExpectedResultResponse> deleteById(@PathVariable String id) {
        return ResponseEntity.ok(expectedResultMapper.deleteById(id));
    }
}
