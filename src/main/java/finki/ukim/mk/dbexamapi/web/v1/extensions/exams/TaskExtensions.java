package finki.ukim.mk.dbexamapi.web.v1.extensions.exams;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.TaskDto;
import finki.ukim.mk.dbexamapi.domain.models.datasets.Dataset;
import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;
import finki.ukim.mk.dbexamapi.domain.models.exams.ExamGroup;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.TaskRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.TaskResponse;

import java.util.List;

public final class TaskExtensions {

    private TaskExtensions() {
    }

    public static TaskDto toDto(TaskRequest request, Exam exam, ExamGroup group, Task sourceTask, Dataset dataset) {
        return new TaskDto(
                exam,
                group,
                request.type(),
                request.dataSource(),
                sourceTask,
                dataset,
                request.solution(),
                request.title(),
                request.description(),
                request.points(),
                request.sortOrder()
        );
    }

    public static TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getExam().getId(),
                task.getGroup() == null ? null : task.getGroup().getId(),
                task.getType(),
                task.getDataSource(),
                task.getSourceTask() == null ? null : task.getSourceTask().getId(),
                task.getDataset() == null ? null : task.getDataset().getId(),
                task.getSolution(),
                task.getTitle(),
                task.getDescription(),
                task.getPoints(),
                task.getSortOrder()
        );
    }

    public static List<TaskResponse> toResponse(List<Task> tasks) {
        return tasks.stream()
                .map(TaskExtensions::toResponse)
                .toList();
    }
}
