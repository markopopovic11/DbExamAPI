package finki.ukim.mk.dbexamapi.helpers.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.DatasetVersionDocumentInvalidException;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.VersionNotFromTaskDatasetException;
import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import finki.ukim.mk.dbexamapi.domain.models.datasets.DatasetVersion;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import org.springframework.stereotype.Component;

/**
 * Rules for dataset versions: where their data lives and which tasks they may test.
 */
@Component
public class DatasetVersionRuleHelper {

    // A version's data is an uploaded .sql file in no folder, so it is never listed to students.
    public void requireSeedFile(Document document) {
        if (document.getFilePath() == null || document.getFolder() != null) {
            throw new DatasetVersionDocumentInvalidException(document.getId());
        }
    }

    // A task is tested and graded only on versions of its own dataset.
    public void requireVersionOfTaskDataset(Task task, DatasetVersion version) {
        if (task.getDataset() == null || !version.getDataset().getId().equals(task.getDataset().getId())) {
            throw new VersionNotFromTaskDatasetException(task.getId(), version.getId());
        }
    }
}
