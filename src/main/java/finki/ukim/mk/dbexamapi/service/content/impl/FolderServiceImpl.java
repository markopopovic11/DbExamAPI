package finki.ukim.mk.dbexamapi.service.content.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.content.FolderDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.FolderDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.models.content.Folder;
import finki.ukim.mk.dbexamapi.repository.content.FolderRepository;
import finki.ukim.mk.dbexamapi.service.content.FolderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;

    public FolderServiceImpl(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    @Override
    public Optional<Folder> findById(String id) {
        return folderRepository.findById(id);
    }

    @Override
    public Folder findByIdNotNull(String id) {
        return folderRepository.findById(id)
                .orElseThrow(() -> new FolderDoesNotExistException(id));
    }

    @Override
    public List<Folder> findAll() {
        return folderRepository.findAll();
    }

    @Override
    @Transactional
    public Folder create(FolderDto folderDto) {
        Folder folder = new Folder();
        folder.setCourse(folderDto.course());
        folder.setName(folderDto.name());

        Folder saved = folderRepository.save(folder);
        log.info("Created folder with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Folder update(String id, FolderDto folderDto) {
        Folder folder = findByIdNotNull(id);

        folder.setCourse(folderDto.course());
        folder.setName(folderDto.name());

        Folder saved = folderRepository.save(folder);
        log.info("Updated folder with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Folder deleteById(String id) {
        Folder folder = findByIdNotNull(id);
        folderRepository.delete(folder);
        log.info("Deleted folder with id: {}", id);
        return folder;
    }
}
