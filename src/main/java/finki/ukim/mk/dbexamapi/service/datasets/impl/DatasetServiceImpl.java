package finki.ukim.mk.dbexamapi.service.datasets.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.DatasetDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.DatasetDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.DatasetExamImmutableException;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.DatasetInUseException;
import finki.ukim.mk.dbexamapi.domain.models.datasets.Dataset;
import finki.ukim.mk.dbexamapi.repository.datasets.DatasetRepository;
import finki.ukim.mk.dbexamapi.service.datasets.DatasetService;
import finki.ukim.mk.dbexamapi.service.datasets.DatasetVersionService;
import finki.ukim.mk.dbexamapi.service.exams.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class DatasetServiceImpl implements DatasetService {

    private final DatasetRepository datasetRepository;
    private final TaskService taskService;
    private final DatasetVersionService datasetVersionService;

    public DatasetServiceImpl(DatasetRepository datasetRepository,
                              TaskService taskService,
                              DatasetVersionService datasetVersionService) {
        this.datasetRepository = datasetRepository;
        this.taskService = taskService;
        this.datasetVersionService = datasetVersionService;
    }

    @Override
    public Optional<Dataset> findById(String id) {
        return datasetRepository.findById(id);
    }

    @Override
    public Dataset findByIdNotNull(String id) {
        return datasetRepository.findById(id)
                .orElseThrow(() -> new DatasetDoesNotExistException(id));
    }

    @Override
    public List<Dataset> findAll() {
        return datasetRepository.findAll();
    }

    @Override
    @Transactional
    public Dataset create(DatasetDto datasetDto) {
        Dataset dataset = new Dataset();
        dataset.setExam(datasetDto.exam());
        dataset.setName(datasetDto.name());
        dataset.setDdl(datasetDto.ddl());

        Dataset saved = datasetRepository.save(dataset);
        log.info("Created dataset with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Dataset update(String id, DatasetDto datasetDto) {
        Dataset dataset = findByIdNotNull(id);

        if (!datasetDto.exam().getId().equals(dataset.getExam().getId())) {
            throw new DatasetExamImmutableException(id);
        }

        dataset.setName(datasetDto.name());
        dataset.setDdl(datasetDto.ddl());

        Dataset saved = datasetRepository.save(dataset);
        log.info("Updated dataset with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Dataset deleteById(String id) {
        Dataset dataset = findByIdNotNull(id);
        if (taskService.existsByDatasetId(id)) {
            throw new DatasetInUseException(id);
        }
        datasetVersionService.deleteAllByDatasetId(id);
        datasetRepository.delete(dataset);
        log.info("Deleted dataset with id: {} and its versions", id);
        return dataset;
    }
}
