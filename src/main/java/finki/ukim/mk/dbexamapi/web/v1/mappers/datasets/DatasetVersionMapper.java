package finki.ukim.mk.dbexamapi.web.v1.mappers.datasets;

import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import finki.ukim.mk.dbexamapi.domain.models.datasets.Dataset;
import finki.ukim.mk.dbexamapi.service.content.DocumentService;
import finki.ukim.mk.dbexamapi.service.datasets.DatasetService;
import finki.ukim.mk.dbexamapi.service.datasets.DatasetVersionService;
import finki.ukim.mk.dbexamapi.web.v1.extensions.datasets.DatasetVersionExtensions;
import finki.ukim.mk.dbexamapi.web.v1.requests.datasets.DatasetVersionRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.datasets.DatasetVersionResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatasetVersionMapper {

    private final DatasetVersionService datasetVersionService;
    private final DatasetService datasetService;
    private final DocumentService documentService;

    public DatasetVersionMapper(DatasetVersionService datasetVersionService,
                                DatasetService datasetService,
                                DocumentService documentService) {
        this.datasetVersionService = datasetVersionService;
        this.datasetService = datasetService;
        this.documentService = documentService;
    }

    public List<DatasetVersionResponse> findAll() {
        return DatasetVersionExtensions.toResponse(datasetVersionService.findAll());
    }

    public DatasetVersionResponse findById(String id) {
        return DatasetVersionExtensions.toResponse(datasetVersionService.findByIdNotNull(id));
    }

    public DatasetVersionResponse create(DatasetVersionRequest request) {
        Dataset dataset = datasetService.findByIdNotNull(request.datasetId());
        Document document = documentService.findByIdNotNull(request.documentId());
        return DatasetVersionExtensions.toResponse(datasetVersionService.create(DatasetVersionExtensions.toDto(request, dataset, document)));
    }

    public DatasetVersionResponse update(String id, DatasetVersionRequest request) {
        Dataset dataset = datasetService.findByIdNotNull(request.datasetId());
        Document document = documentService.findByIdNotNull(request.documentId());
        return DatasetVersionExtensions.toResponse(datasetVersionService.update(id, DatasetVersionExtensions.toDto(request, dataset, document)));
    }

    public DatasetVersionResponse deleteById(String id) {
        return DatasetVersionExtensions.toResponse(datasetVersionService.deleteById(id));
    }
}
