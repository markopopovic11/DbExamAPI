package finki.ukim.mk.dbexamapi.web.v1.mappers.content;

import finki.ukim.mk.dbexamapi.domain.models.exams.Course;
import finki.ukim.mk.dbexamapi.service.content.FolderService;
import finki.ukim.mk.dbexamapi.service.exams.CourseService;
import finki.ukim.mk.dbexamapi.web.v1.extensions.content.FolderExtensions;
import finki.ukim.mk.dbexamapi.web.v1.requests.content.FolderRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.content.FolderResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FolderMapper {

    private final FolderService folderService;
    private final CourseService courseService;

    public FolderMapper(FolderService folderService,
                        CourseService courseService) {
        this.folderService = folderService;
        this.courseService = courseService;
    }

    public List<FolderResponse> findAll() {
        return FolderExtensions.toResponse(folderService.findAll());
    }

    public FolderResponse findById(String id) {
        return FolderExtensions.toResponse(folderService.findByIdNotNull(id));
    }

    public FolderResponse create(FolderRequest request) {
        Course course = courseService.findByIdNotNull(request.courseId());
        return FolderExtensions.toResponse(folderService.create(FolderExtensions.toDto(request, course)));
    }

    public FolderResponse update(String id, FolderRequest request) {
        Course course = courseService.findByIdNotNull(request.courseId());
        return FolderExtensions.toResponse(folderService.update(id, FolderExtensions.toDto(request, course)));
    }

    public FolderResponse deleteById(String id) {
        return FolderExtensions.toResponse(folderService.deleteById(id));
    }
}
