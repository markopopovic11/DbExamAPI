package finki.ukim.mk.dbexamapi.service.auth.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.auth.AuthDto;
import finki.ukim.mk.dbexamapi.domain.dtos.auth.RegisterDto;
import finki.ukim.mk.dbexamapi.domain.dtos.auth.TokenWrapperDto;
import finki.ukim.mk.dbexamapi.domain.dtos.identity.UserDetailDto;
import finki.ukim.mk.dbexamapi.domain.dtos.identity.UserDto;
import finki.ukim.mk.dbexamapi.domain.enums.TokenType;
import finki.ukim.mk.dbexamapi.domain.enums.UserRole;
import finki.ukim.mk.dbexamapi.domain.exceptions.auth.InvalidCredentialsException;
import finki.ukim.mk.dbexamapi.domain.exceptions.auth.InvalidTokenException;
import finki.ukim.mk.dbexamapi.domain.exceptions.auth.PasswordsDoNotMatchException;
import finki.ukim.mk.dbexamapi.domain.models.identity.User;
import finki.ukim.mk.dbexamapi.domain.models.identity.UserDetail;
import finki.ukim.mk.dbexamapi.service.auth.AuthService;
import finki.ukim.mk.dbexamapi.service.auth.TokenService;
import finki.ukim.mk.dbexamapi.service.identity.UserDetailService;
import finki.ukim.mk.dbexamapi.service.identity.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserDetailService userDetailService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserService userService,
                           UserDetailService userDetailService,
                           TokenService tokenService,
                           PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userDetailService = userDetailService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AuthDto register(RegisterDto registerDto) {
        if (!registerDto.password().equals(registerDto.confirmPassword())) {
            throw new PasswordsDoNotMatchException();
        }

        User user = userService.create(new UserDto(UserRole.STUDENT));
        UserDetail userDetail = userDetailService.create(new UserDetailDto(
                user,
                registerDto.firstName(),
                registerDto.lastName(),
                registerDto.index(),
                registerDto.email()
        ));
        userDetailService.updatePassword(userDetail.getId(), passwordEncoder.encode(registerDto.password()));

        log.info("Registered user with id: {}", user.getId());
        return new AuthDto(user, tokenService.generateAuthTokens(user));
    }

    @Override
    public AuthDto login(String email, String password) {
        UserDetail userDetail = userDetailService.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (userDetail.getPassword() == null || !passwordEncoder.matches(password, userDetail.getPassword())) {
            throw new InvalidCredentialsException();
        }

        User user = userDetail.getUser();
        return new AuthDto(user, tokenService.generateAuthTokens(user));
    }

    @Override
    public TokenWrapperDto refresh(String refreshToken) {
        String userId = tokenService.verifyToken(refreshToken, TokenType.REFRESH).userId();

        User user = userService.findById(userId)
                .orElseThrow(InvalidTokenException::new);

        return tokenService.generateAuthTokens(user);
    }
}
