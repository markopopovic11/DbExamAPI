package finki.ukim.mk.dbexamapi.web.v1.extensions.datasets;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.DatasetDto;
import finki.ukim.mk.dbexamapi.domain.models.datasets.Dataset;
import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;
import finki.ukim.mk.dbexamapi.web.v1.requests.datasets.DatasetRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.datasets.DatasetResponse;

import java.util.List;

public final class DatasetExtensions {

    private DatasetExtensions() {
    }

    public static DatasetDto toDto(DatasetRequest request, Exam exam) {
        return new DatasetDto(
                exam,
                request.name(),
                request.ddl()
        );
    }

    public static DatasetResponse toResponse(Dataset dataset) {
        return new DatasetResponse(
                dataset.getId(),
                dataset.getExam().getId(),
                dataset.getName(),
                dataset.getDdl()
        );
    }

    public static List<DatasetResponse> toResponse(List<Dataset> datasets) {
        return datasets.stream()
                .map(DatasetExtensions::toResponse)
                .toList();
    }
}
