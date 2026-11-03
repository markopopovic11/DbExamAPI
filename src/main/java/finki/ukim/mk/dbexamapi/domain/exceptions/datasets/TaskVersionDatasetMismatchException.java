package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class TaskVersionDatasetMismatchException extends RuleViolationException {

    public TaskVersionDatasetMismatchException(String taskId, String versionId) {
        super(String.format("Version with id %s is not from the dataset of task with id %s.", versionId, taskId));
    }
}
