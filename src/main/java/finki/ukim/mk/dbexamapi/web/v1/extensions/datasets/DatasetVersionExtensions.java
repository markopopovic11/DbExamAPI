package finki.ukim.mk.dbexamapi.web.v1.extensions.datasets;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.DatasetVersionDto;
import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import finki.ukim.mk.dbexamapi.domain.models.datasets.Dataset;
import finki.ukim.mk.dbexamapi.domain.models.datasets.DatasetVersion;
import finki.ukim.mk.dbexamapi.web.v1.requests.datasets.DatasetVersionRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.datasets.DatasetVersionResponse;

import java.util.List;

public final class DatasetVersionExtensions {

    private DatasetVersionExtensions() {
    }

    public static DatasetVersionDto toDto(DatasetVersionRequest request, Dataset dataset, Document document) {
        return new DatasetVersionDto(
                dataset,
                request.name(),
                request.visible(),
                document,
                request.sortOrder()
        );
    }

    public static DatasetVersionResponse toResponse(DatasetVersion datasetVersion) {
        return new DatasetVersionResponse(
                datasetVersion.getId(),
                datasetVersion.getDataset().getId(),
                datasetVersion.getName(),
                datasetVersion.isVisible(),
                datasetVersion.getDocument().getId(),
                datasetVersion.getSortOrder(),
                datasetVersion.getUpdatedAt()
        );
    }

    public static List<DatasetVersionResponse> toResponse(List<DatasetVersion> datasetVersions) {
        return datasetVersions.stream()
                .map(DatasetVersionExtensions::toResponse)
                .toList();
    }
}
