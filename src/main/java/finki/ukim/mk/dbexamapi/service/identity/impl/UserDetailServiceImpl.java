package finki.ukim.mk.dbexamapi.service.identity.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.identity.UserDetailDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.identity.UserDetailAlreadyExistsForUserException;
import finki.ukim.mk.dbexamapi.domain.exceptions.identity.UserDetailDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.exceptions.identity.UserDetailEmailAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.exceptions.identity.UserDetailIndexAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.models.identity.UserDetail;
import finki.ukim.mk.dbexamapi.repository.identity.UserDetailRepository;
import finki.ukim.mk.dbexamapi.service.identity.UserDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserDetailServiceImpl implements UserDetailService {

    private final UserDetailRepository userDetailRepository;

    public UserDetailServiceImpl(UserDetailRepository userDetailRepository) {
        this.userDetailRepository = userDetailRepository;
    }

    @Override
    public Optional<UserDetail> findById(String id) {
        return userDetailRepository.findById(id);
    }

    @Override
    public UserDetail findByIdNotNull(String id) {
        return userDetailRepository.findById(id)
                .orElseThrow(() -> new UserDetailDoesNotExistException(id));
    }

    @Override
    public List<UserDetail> findAll() {
        return userDetailRepository.findAll();
    }

    @Override
    @Transactional
    public UserDetail create(UserDetailDto userDetailDto) {
        if (userDetailRepository.existsByUser_Id(userDetailDto.user().getId())) {
            throw new UserDetailAlreadyExistsForUserException(userDetailDto.user().getId());
        }
        if (userDetailDto.index() != null && userDetailRepository.existsByIndex(userDetailDto.index())) {
            throw new UserDetailIndexAlreadyExistsException(userDetailDto.index());
        }
        if (userDetailDto.email() != null && userDetailRepository.existsByEmail(userDetailDto.email())) {
            throw new UserDetailEmailAlreadyExistsException(userDetailDto.email());
        }

        UserDetail userDetail = new UserDetail();
        userDetail.setUser(userDetailDto.user());
        userDetail.setFirstName(userDetailDto.firstName());
        userDetail.setLastName(userDetailDto.lastName());
        userDetail.setIndex(userDetailDto.index());
        userDetail.setEmail(userDetailDto.email());

        UserDetail saved = userDetailRepository.save(userDetail);
        log.info("Created user detail with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public UserDetail update(String id, UserDetailDto userDetailDto) {
        UserDetail userDetail = findByIdNotNull(id);

        if (userDetailRepository.existsByUser_IdAndIdNot(userDetailDto.user().getId(), id)) {
            throw new UserDetailAlreadyExistsForUserException(userDetailDto.user().getId());
        }
        if (userDetailDto.index() != null && userDetailRepository.existsByIndexAndIdNot(userDetailDto.index(), id)) {
            throw new UserDetailIndexAlreadyExistsException(userDetailDto.index());
        }
        if (userDetailDto.email() != null && userDetailRepository.existsByEmailAndIdNot(userDetailDto.email(), id)) {
            throw new UserDetailEmailAlreadyExistsException(userDetailDto.email());
        }

        userDetail.setUser(userDetailDto.user());
        userDetail.setFirstName(userDetailDto.firstName());
        userDetail.setLastName(userDetailDto.lastName());
        userDetail.setIndex(userDetailDto.index());
        userDetail.setEmail(userDetailDto.email());

        UserDetail saved = userDetailRepository.save(userDetail);
        log.info("Updated user detail with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public UserDetail deleteById(String id) {
        UserDetail userDetail = findByIdNotNull(id);
        userDetailRepository.delete(userDetail);
        log.info("Deleted user detail with id: {}", id);
        return userDetail;
    }

    @Override
    public Optional<UserDetail> findByEmail(String email) {
        return userDetailRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public UserDetail updatePassword(String id, String encodedPassword) {
        UserDetail userDetail = findByIdNotNull(id);
        userDetail.setPassword(encodedPassword);

        UserDetail saved = userDetailRepository.save(userDetail);
        log.info("Updated password of user detail with id: {}", saved.getId());
        return saved;
    }
}
