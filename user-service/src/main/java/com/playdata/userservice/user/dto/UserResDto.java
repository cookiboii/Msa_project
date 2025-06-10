package com.playdata.userservice.user.dto;

import com.playdata.userservice.user.entity.Role;
import lombok.*;

import java.io.Serializable;

@Builder
public record UserResDto (  String email,Long id,String name,Role role ,String profileImage, String socialProvider)  {








}