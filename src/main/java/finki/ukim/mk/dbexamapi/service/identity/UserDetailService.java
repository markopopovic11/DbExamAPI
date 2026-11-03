package finki.ukim.mk.dbexamapi.service.identity;

import finki.ukim.mk.dbexamapi.domain.dtos.identity.UserDetailDto;
import finki.ukim.mk.dbexamapi.domain.models.identity.UserDetail;

import java.util.List;
import java.util.Optional;

public interface UserDetailService {

    Optional<UserDetail> findById(String id);

    UserDetail findByIdNotNull(String id);

    List<UserDetail> findAll();

    UserDetail create(UserDetailDto userDetailDto);

    UserDetail update(String id, UserDetailDto userDetailDto);

    UserDetail deleteById(String id);

    Optional<UserDetail> findByEmail(String email);

    /**
     * Takes the already encoded password.
     */
    UserDetail updatePassword(String id, String encodedPassword);
}
