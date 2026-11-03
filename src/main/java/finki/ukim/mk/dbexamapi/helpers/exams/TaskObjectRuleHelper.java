package finki.ukim.mk.dbexamapi.helpers.exams;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.TaskObjectDto;
import finki.ukim.mk.dbexamapi.domain.enums.TaskObjectKind;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskObjectFieldsInvalidException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskObjectKindInvalidException;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import org.springframework.stereotype.Component;

/**
 * Rules for the objects a task asks the student to create.
 */
@Component
public class TaskObjectRuleHelper {

    // QUERY: a VIEW. CODE: functions, procedures and triggers. Other task types have no objects.
    public void requireKindFitsTask(TaskObjectDto taskObjectDto) {
        Task task = taskObjectDto.task();
        TaskObjectKind kind = taskObjectDto.kind();

        boolean kindFits = switch (task.getType()) {
            case QUERY -> kind == TaskObjectKind.VIEW;
            case CODE -> kind != TaskObjectKind.VIEW;
            default -> false;
        };
        if (!kindFits) {
            throw new TaskObjectKindInvalidException(task.getType().name(), kind.name());
        }
    }

    // Only functions and procedures take arguments; a trigger needs its table and nothing else has one.
    public void requireFieldsFitKind(TaskObjectDto taskObjectDto) {
        TaskObjectKind kind = taskObjectDto.kind();

        boolean takesArguments = kind == TaskObjectKind.FUNCTION || kind == TaskObjectKind.PROCEDURE;
        if (!takesArguments && taskObjectDto.arguments() != null) {
            throw new TaskObjectFieldsInvalidException("only functions and procedures have arguments");
        }
        if ((kind == TaskObjectKind.TRIGGER) != (taskObjectDto.onTable() != null)) {
            throw new TaskObjectFieldsInvalidException("a trigger needs its table and other objects have none");
        }
    }
}
