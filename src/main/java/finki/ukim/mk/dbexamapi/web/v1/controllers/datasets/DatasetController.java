package finki.ukim.mk.dbexamapi.web.v1.controllers.datasets;

import finki.ukim.mk.dbexamapi.web.v1.mappers.datasets.DatasetMapper;
import finki.ukim.mk.dbexamapi.web.v1.requests.datasets.DatasetRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.datasets.DatasetResponse;
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
@RequestMapping("/v1/datasets")
public class DatasetController {

    private final DatasetMapper datasetMapper;

    public DatasetController(DatasetMapper datasetMapper) {
        this.datasetMapper = datasetMapper;
    }

    @GetMapping
    public ResponseEntity<List<DatasetResponse>> findAll() {
        return ResponseEntity.ok(datasetMapper.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatasetResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(datasetMapper.findById(id));
    }

    @PostMapping
    public ResponseEntity<DatasetResponse> create(@Valid @RequestBody DatasetRequest request) {
        return ResponseEntity.ok(datasetMapper.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatasetResponse> update(@PathVariable String id,
                                                  @Valid @RequestBody DatasetRequest request) {
        return ResponseEntity.ok(datasetMapper.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DatasetResponse> deleteById(@PathVariable String id) {
        return ResponseEntity.ok(datasetMapper.deleteById(id));
    }
}
