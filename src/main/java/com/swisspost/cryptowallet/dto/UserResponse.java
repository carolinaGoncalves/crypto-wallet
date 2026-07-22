package com.swisspost.cryptowallet.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserResponse {

    private String username;
    private String fullName;
    private LocalDateTime createdAt;

}
