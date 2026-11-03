package finki.ukim.mk.dbexamapi.helpers.exams;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.TaskDto;
import finki.ukim.mk.dbexamapi.domain.enums.TaskType;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskDataSourceInvalidException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskGroupExamMismatchException;
import org.springframework.stereotype.Component;

/**
 * Rules for what a task points at: its group and the data it runs on.
 */
@Component
public class TaskRuleHelper {

    // A task in a group must be in a group of its own exam. No group = shared by every group.
    public void requireGroupInExam(TaskDto taskDto) {
        String examId = taskDto.exam().getId();
        if (taskDto.group() != null && !taskDto.group().getExam().getId().equals(examId)) {
            throw new TaskGroupExamMismatchException(taskDto.group().getId(), examId);
        }
    }

    // Only QUERY and CODE run on data. DATASETS needs a dataset of the same exam,
    // OWN_SCHEMA needs an earlier DDL task of the same exam.
    public void requireDataSourceFits(TaskDto taskDto) {
        boolean runsOnData = taskDto.type() == TaskType.QUERY || taskDto.type() == TaskType.CODE;

        if (!runsOnData) {
            if (taskDto.dataSource() != null || taskDto.sourceTask() != null || taskDto.dataset() != null) {
                throw new TaskDataSourceInvalidException("only QUERY and CODE tasks have a data source");
            }
            return;
        }
        if (taskDto.dataSource() == null) {
            throw new TaskDataSourceInvalidException("QUERY and CODE tasks need a data source");
        }

        String examId = taskDto.exam().getId();
        switch (taskDto.dataSource()) {
            case DATASETS -> {
                if (taskDto.dataset() == null || taskDto.sourceTask() != null) {
                    throw new TaskDataSourceInvalidException("DATASETS needs a dataset and no source task");
                }
                if (!taskDto.dataset().getExam().getId().equals(examId)) {
                    throw new TaskDataSourceInvalidException("the dataset must belong to the task's exam");
                }
            }
            case OWN_SCHEMA -> {
                if (taskDto.sourceTask() == null || taskDto.dataset() != null) {
                    throw new TaskDataSourceInvalidException("OWN_SCHEMA needs a source task and no dataset");
                }
                if (taskDto.sourceTask().getType() != TaskType.DDL) {
                    throw new TaskDataSourceInvalidException("the source task must be a DDL task");
                }
                if (!taskDto.sourceTask().getExam().getId().equals(examId)) {
                    throw new TaskDataSourceInvalidException("the source task must belong to the task's exam");
                }
            }
        }
    }
}
