package finki.ukim.mk.dbexamapi.service.datasets.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.DatasetVersionDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.DatasetVersionDatasetImmutableException;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.DatasetVersionDocumentInvalidException;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.DatasetVersionDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.DatasetVersionInUseException;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.DatasetVersionNameAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.DatasetVersionVisibleAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import finki.ukim.mk.dbexamapi.domain.models.datasets.DatasetVersion;
import finki.ukim.mk.dbexamapi.repository.datasets.DatasetVersionRepository;
import finki.ukim.mk.dbexamapi.service.content.DocumentService;
import finki.ukim.mk.dbexamapi.service.datasets.DatasetVersionService;
import finki.ukim.mk.dbexamapi.service.datasets.TaskVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class DatasetVersionServiceImpl implements DatasetVersionService {

    private final DatasetVersionRepository datasetVersionRepository;
    private final TaskVersionService taskVersionService;
    private final DocumentService documentService;

    public DatasetVersionServiceImpl(DatasetVersionRepository datasetVersionRepository,
                                     TaskVersionService taskVersionService,
                                     DocumentService documentService) {
        this.datasetVersionRepository = datasetVersionRepository;
        this.taskVersionService = taskVersionService;
        this.documentService = documentService;
    }

    @Override
    public Optional<DatasetVersion> findById(String id) {
        return datasetVersionRepository.findById(id);
    }

    @Override
    public DatasetVersion findByIdNotNull(String id) {
        return datasetVersionRepository.findById(id)
                .orElseThrow(() -> new DatasetVersionDoesNotExistException(id));
    }

    @Override
    public List<DatasetVersion> findAll() {
        return datasetVersionRepository.findAll();
    }

    @Override
    @Transactional
    public DatasetVersion create(DatasetVersionDto datasetVersionDto) {
        String datasetId = datasetVersionDto.dataset().getId();
        if (datasetVersionRepository.existsByDataset_IdAndName(datasetId, datasetVersionDto.name())) {
            throw new DatasetVersionNameAlreadyExistsException(datasetId, datasetVersionDto.name());
        }
        if (datasetVersionDto.visible() && datasetVersionRepository.existsByDataset_IdAndVisibleTrue(datasetId)) {
            throw new DatasetVersionVisibleAlreadyExistsException(datasetId);
        }
        validateDocument(datasetVersionDto.document());

        DatasetVersion datasetVersion = new DatasetVersion();
        datasetVersion.setDataset(datasetVersionDto.dataset());
        datasetVersion.setName(datasetVersionDto.name());
        datasetVersion.setVisible(datasetVersionDto.visible());
        datasetVersion.setDocument(datasetVersionDto.document());
        datasetVersion.setSortOrder(datasetVersionDto.sortOrder());

        DatasetVersion saved = datasetVersionRepository.save(datasetVersion);
        log.info("Created dataset version with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public DatasetVersion update(String id, DatasetVersionDto datasetVersionDto) {
        DatasetVersion datasetVersion = findByIdNotNull(id);

        String datasetId = datasetVersion.getDataset().getId();
        if (!datasetVersionDto.dataset().getId().equals(datasetId)) {
            throw new DatasetVersionDatasetImmutableException(id);
        }
        if (datasetVersionRepository.existsByDataset_IdAndNameAndIdNot(datasetId, datasetVersionDto.name(), id)) {
            throw new DatasetVersionNameAlreadyExistsException(datasetId, datasetVersionDto.name());
        }
        if (datasetVersionDto.visible()
                && datasetVersionRepository.existsByDataset_IdAndVisibleTrueAndIdNot(datasetId, id)) {
            throw new DatasetVersionVisibleAlreadyExistsException(datasetId);
        }
        validateDocument(datasetVersionDto.document());

        datasetVersion.setName(datasetVersionDto.name());
        datasetVersion.setVisible(datasetVersionDto.visible());
        datasetVersion.setDocument(datasetVersionDto.document());
        datasetVersion.setSortOrder(datasetVersionDto.sortOrder());

        DatasetVersion saved = datasetVersionRepository.save(datasetVersion);
        log.info("Updated dataset version with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public DatasetVersion deleteById(String id) {
        DatasetVersion datasetVersion = findByIdNotNull(id);
        if (taskVersionService.existsByVersionId(id)) {
            throw new DatasetVersionInUseException(id);
        }
        datasetVersionRepository.delete(datasetVersion);
        documentService.deleteById(datasetVersion.getDocument().getId());
        log.info("Deleted dataset version with id: {} and its data document", id);
        return datasetVersion;
    }

    @Override
    @Transactional
    public void deleteAllByDatasetId(String datasetId) {
        for (DatasetVersion datasetVersion : datasetVersionRepository.findAllByDataset_Id(datasetId)) {
            deleteById(datasetVersion.getId());
        }
        log.info("Deleted versions of dataset with id: {}", datasetId);
    }

    private void validateDocument(Document document) {
        if (document.getFilePath() == null || document.getFolder() != null) {
            throw new DatasetVersionDocumentInvalidException(document.getId());
        }
    }
}
