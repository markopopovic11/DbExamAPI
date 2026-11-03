package finki.ukim.mk.dbexamapi.service.datasets;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.DatasetVersionDto;
import finki.ukim.mk.dbexamapi.domain.models.datasets.DatasetVersion;

import java.util.List;
import java.util.Optional;

public interface DatasetVersionService {

    Optional<DatasetVersion> findById(String id);

    DatasetVersion findByIdNotNull(String id);

    List<DatasetVersion> findAll();

    DatasetVersion create(DatasetVersionDto datasetVersionDto);

    DatasetVersion update(String id, DatasetVersionDto datasetVersionDto);

    /**
     * Also deletes the version's data document.
     */
    DatasetVersion deleteById(String id);

    /**
     * Also deletes the versions' data documents.
     */
    void deleteAllByDatasetId(String datasetId);
}
