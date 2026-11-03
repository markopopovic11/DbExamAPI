package finki.ukim.mk.dbexamapi.helpers.datasets;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.ExpectedResultDto;
import finki.ukim.mk.dbexamapi.domain.enums.TaskType;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.ExpectedResultStepInvalidException;
import org.springframework.stereotype.Component;

/**
 * Rules for which step an expected result belongs to.
 */
@Component
public class ExpectedResultRuleHelper {

    // QUERY: one result per version, no step. CODE: one result per version and step of the same task.
    public void requireStepFitsTask(ExpectedResultDto expectedResultDto) {
        if (expectedResultDto.task().getType() == TaskType.QUERY) {
            if (expectedResultDto.step() != null) {
                throw new ExpectedResultStepInvalidException("QUERY results have no step");
            }
            return;
        }
        if (expectedResultDto.step() == null
                || !expectedResultDto.step().getTask().getId().equals(expectedResultDto.task().getId())) {
            throw new ExpectedResultStepInvalidException("CODE results need a step of the same task");
        }
    }
}
