package finki.ukim.mk.dbexamapi.web.v1.controllers.datasets;

import finki.ukim.mk.dbexamapi.web.v1.mappers.datasets.DatasetVersionMapper;
import finki.ukim.mk.dbexamapi.web.v1.requests.datasets.DatasetVersionRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.datasets.DatasetVersionResponse;
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
@RequestMapping("/v1/dataset-versions")
public class DatasetVersionController {

    private final DatasetVersionMapper datasetVersionMapper;

    public DatasetVersionController(DatasetVersionMapper datasetVersionMapper) {
        this.datasetVersionMapper = datasetVersionMapper;
    }

    @GetMapping
    public ResponseEntity<List<DatasetVersionResponse>> findAll() {
        return ResponseEntity.ok(datasetVersionMapper.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatasetVersionResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(datasetVersionMapper.findById(id));
    }

    @PostMapping
    public ResponseEntity<DatasetVersionResponse> create(@Valid @RequestBody DatasetVersionRequest request) {
        return ResponseEntity.ok(datasetVersionMapper.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatasetVersionResponse> update(@PathVariable String id,
                                                         @Valid @RequestBody DatasetVersionRequest request) {
        return ResponseEntity.ok(datasetVersionMapper.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DatasetVersionResponse> deleteById(@PathVariable String id) {
        return ResponseEntity.ok(datasetVersionMapper.deleteById(id));
    }
}
