package com.swisspost.cryptowallet.dto;


import com.swisspost.cryptowallet.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserResponse {

    private String username;
    private String fullName;
    private LocalDateTime createdAt;

    public static UserResponse mapUserToUserDto(User user) {
        return new UserResponse(
                user.getUsername(),
                user.getFullName(),
                user.getCreatedAt()
        );
    }

    public static User mapUserDtoToUser(UserRequest user) {
        return new User(
                user.getUsername(),
                user.getFullName(),
                LocalDateTime.now()
        );
    }

}
