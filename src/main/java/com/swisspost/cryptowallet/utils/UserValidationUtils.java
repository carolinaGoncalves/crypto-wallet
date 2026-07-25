package com.swisspost.cryptowallet.utils;

import com.swisspost.cryptowallet.entity.User;
import com.swisspost.cryptowallet.exception.UserNotFoundException;
import com.swisspost.cryptowallet.repository.UserRepository;

public class UserValidationUtils {

    public static User getUserIfExists(UserRepository userRepository, String username){
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(username));
    }

}
