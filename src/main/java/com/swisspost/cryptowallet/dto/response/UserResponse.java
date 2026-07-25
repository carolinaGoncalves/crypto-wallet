package com.swisspost.cryptowallet.dto.response;


import com.swisspost.cryptowallet.dto.request.UserRequest;
import com.swisspost.cryptowallet.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
                LocalDateTime.now(ZoneOffset.UTC)
        );
    }

}
