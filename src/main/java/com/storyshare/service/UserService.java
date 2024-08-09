package com.storyshare.service;

import com.amazonaws.services.cloudformation.model.AlreadyExistsException;
import com.storyshare.dto.request.UserLoginRequest;
import com.storyshare.dto.request.UserRegisterRequest;
import com.storyshare.dto.response.JwtResponse;
import com.storyshare.dto.response.UserResponse;
import com.storyshare.entity.RoleEntity;
import com.storyshare.entity.UserEntity;
import com.storyshare.exception.NotFoundException;
import com.storyshare.mapper.UserMapper;
import com.storyshare.repository.RoleRepository;
import com.storyshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserResponse register(UserRegisterRequest request){
        log.info("ActionLog.register.start for username {}", request.getUsername());
        userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
            log.error("ActionLog.register.username.AlreadyExistsException for username {}", user.getUsername());
            throw new AlreadyExistsException("USERNAME_ALREADY_EXISTS");
        });
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            log.error("ActionLog.register.email.AlreadyExistsException for username {}", user.getUsername());
            throw  new AlreadyExistsException("EMAIL_ALREADY_EXISTS");
        });
        UserEntity entity = UserMapper.INSTANCE.registerRequestToEntity(request);
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity.setRoles(List.of(getRole()));
        userRepository.save(entity);
        log.info("ActionLog.register.end for username {}", entity.getUsername());
        return UserMapper.INSTANCE.entityToResponse(entity);
    }

    public JwtResponse login(UserLoginRequest request){
        log.info("ActionLog.login.start for username {}", request.getUsername());
        UserEntity entity = userRepository.findByUsername(request.getUsername()).orElseThrow(()-> {
            log.error("ActionLog.login.user.NotFoundException for username {}", request.getUsername());
            return new NotFoundException("USER_NOT_FOUND");
        });
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User principal = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(principal);
        log.info("ActionLog.login.end for username {}", request.getUsername());
        return new JwtResponse(request.getUsername(), accessToken);
    }

    private RoleEntity getRole() {
        return roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(RoleEntity.builder().name("USER").build()));
    }
}
