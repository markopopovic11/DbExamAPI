package finki.ukim.mk.dbexamapi.helpers.exams;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.TestStepDto;
import finki.ukim.mk.dbexamapi.domain.enums.TaskType;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TestStepCheckRequiredException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TestStepTaskNotCodeException;
import org.springframework.stereotype.Component;

/**
 * Rules for a CODE task's test steps.
 */
@Component
public class TestStepRuleHelper {

    public void requireCodeTask(TestStepDto testStepDto) {
        if (testStepDto.task().getType() != TaskType.CODE) {
            throw new TestStepTaskNotCodeException(testStepDto.task().getId());
        }
    }

    // A step checks the table after its action. Only an action that must fail may skip the check.
    public void requireCheckUnlessErrorExpected(TestStepDto testStepDto) {
        if (testStepDto.checkSql() == null && !testStepDto.expectError()) {
            throw new TestStepCheckRequiredException();
        }
    }
}
