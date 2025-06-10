package com.playdata.userservice.user.dto;


import com.playdata.userservice.user.entity.Role;
import lombok.*;


public record UserInfoDto (   String username,
       String email,
         Role role) {




}
