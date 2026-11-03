package finki.ukim.mk.dbexamapi.web.v1.mappers.datasets;

import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;
import finki.ukim.mk.dbexamapi.service.datasets.DatasetService;
import finki.ukim.mk.dbexamapi.service.exams.ExamService;
import finki.ukim.mk.dbexamapi.web.v1.extensions.datasets.DatasetExtensions;
import finki.ukim.mk.dbexamapi.web.v1.requests.datasets.DatasetRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.datasets.DatasetResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatasetMapper {

    private final DatasetService datasetService;
    private final ExamService examService;

    public DatasetMapper(DatasetService datasetService,
                         ExamService examService) {
        this.datasetService = datasetService;
        this.examService = examService;
    }

    public List<DatasetResponse> findAll() {
        return DatasetExtensions.toResponse(datasetService.findAll());
    }

    public DatasetResponse findById(String id) {
        return DatasetExtensions.toResponse(datasetService.findByIdNotNull(id));
    }

    public DatasetResponse create(DatasetRequest request) {
        Exam exam = examService.findByIdNotNull(request.examId());
        return DatasetExtensions.toResponse(datasetService.create(DatasetExtensions.toDto(request, exam)));
    }

    public DatasetResponse update(String id, DatasetRequest request) {
        Exam exam = examService.findByIdNotNull(request.examId());
        return DatasetExtensions.toResponse(datasetService.update(id, DatasetExtensions.toDto(request, exam)));
    }

    public DatasetResponse deleteById(String id) {
        return DatasetExtensions.toResponse(datasetService.deleteById(id));
    }
}
