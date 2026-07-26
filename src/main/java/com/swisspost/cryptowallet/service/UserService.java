package com.swisspost.cryptowallet.service;

import com.swisspost.cryptowallet.dto.request.UserRequest;
import com.swisspost.cryptowallet.dto.response.UserResponse;
import com.swisspost.cryptowallet.entity.User;
import com.swisspost.cryptowallet.repository.UserRepository;
import com.swisspost.cryptowallet.utils.UserValidationUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.swisspost.cryptowallet.dto.response.UserResponse.mapUserDtoToUser;
import static com.swisspost.cryptowallet.dto.response.UserResponse.mapUserToUserDto;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public UserResponse createUser(@Valid UserRequest userRequest) {
        User user = mapUserDtoToUser(userRequest);
        user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        User saved = userRepository.save(user);
        log.info("User {} created successfully", user.getUsername());
        return mapUserToUserDto(saved);
    }

    public UserResponse findUserByUsername(String username){
        User user = UserValidationUtils.getUserIfExists(userRepository, username);
        log.info("User {} found", username);
        return mapUserToUserDto(user);

    }

}
