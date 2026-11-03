package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class TaskObjectKindInvalidException extends RuleViolationException {

    public TaskObjectKindInvalidException(String taskType, String kind) {
        super(String.format("A %s task cannot have a %s object.", taskType, kind));
    }
}
