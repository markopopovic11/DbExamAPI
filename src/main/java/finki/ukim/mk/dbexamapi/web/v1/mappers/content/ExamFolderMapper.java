package finki.ukim.mk.dbexamapi.web.v1.mappers.content;

import finki.ukim.mk.dbexamapi.domain.models.content.Folder;
import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;
import finki.ukim.mk.dbexamapi.service.content.ExamFolderService;
import finki.ukim.mk.dbexamapi.service.content.FolderService;
import finki.ukim.mk.dbexamapi.service.exams.ExamService;
import finki.ukim.mk.dbexamapi.web.v1.extensions.content.ExamFolderExtensions;
import finki.ukim.mk.dbexamapi.web.v1.requests.content.ExamFolderRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.content.ExamFolderResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExamFolderMapper {

    private final ExamFolderService examFolderService;
    private final ExamService examService;
    private final FolderService folderService;

    public ExamFolderMapper(ExamFolderService examFolderService,
                            ExamService examService,
                            FolderService folderService) {
        this.examFolderService = examFolderService;
        this.examService = examService;
        this.folderService = folderService;
    }

    public List<ExamFolderResponse> findAll() {
        return ExamFolderExtensions.toResponse(examFolderService.findAll());
    }

    public ExamFolderResponse findById(String id) {
        return ExamFolderExtensions.toResponse(examFolderService.findByIdNotNull(id));
    }

    public ExamFolderResponse create(ExamFolderRequest request) {
        Exam exam = examService.findByIdNotNull(request.examId());
        Folder folder = folderService.findByIdNotNull(request.folderId());
        return ExamFolderExtensions.toResponse(examFolderService.create(ExamFolderExtensions.toDto(request, exam, folder)));
    }

    public ExamFolderResponse update(String id, ExamFolderRequest request) {
        Exam exam = examService.findByIdNotNull(request.examId());
        Folder folder = folderService.findByIdNotNull(request.folderId());
        return ExamFolderExtensions.toResponse(examFolderService.update(id, ExamFolderExtensions.toDto(request, exam, folder)));
    }

    public ExamFolderResponse deleteById(String id) {
        return ExamFolderExtensions.toResponse(examFolderService.deleteById(id));
    }
}
