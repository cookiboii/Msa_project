package com.playdata.userservice.user.dto;


import com.playdata.userservice.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponseDto {
    private String username;
    private String email;
    private Role role;
}
