package com.swisspost.cryptowallet.service;

import com.swisspost.cryptowallet.dto.UserRequest;
import com.swisspost.cryptowallet.dto.UserResponse;
import com.swisspost.cryptowallet.entity.User;
import com.swisspost.cryptowallet.exception.UserNotFoundException;
import com.swisspost.cryptowallet.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.swisspost.cryptowallet.dto.UserResponse.mapUserDtoToUser;
import static com.swisspost.cryptowallet.dto.UserResponse.mapUserToUserDto;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }



    public UserResponse createUser(@Valid UserRequest userRequest) {
        User user = mapUserDtoToUser(userRequest);
        user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        User saved = userRepository.save(user);
        return mapUserToUserDto(saved);
    }


    public UserResponse findUserByUsername(String username){
       Optional<User> user =  userRepository.findByUsername(username);

        if(user.isPresent()){
            return mapUserToUserDto(user.get());
        }

        throw new UserNotFoundException(username);
    }

}
