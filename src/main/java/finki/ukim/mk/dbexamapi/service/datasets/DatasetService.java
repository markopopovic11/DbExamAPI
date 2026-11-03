package finki.ukim.mk.dbexamapi.service.datasets;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.DatasetDto;
import finki.ukim.mk.dbexamapi.domain.models.datasets.Dataset;

import java.util.List;
import java.util.Optional;

public interface DatasetService {

    Optional<Dataset> findById(String id);

    Dataset findByIdNotNull(String id);

    List<Dataset> findAll();

    Dataset create(DatasetDto datasetDto);

    Dataset update(String id, DatasetDto datasetDto);

    /**
     * Also deletes the dataset's versions and their data documents.
     */
    Dataset deleteById(String id);
}
