package com.playdata.userservice.user.dto;


import com.playdata.userservice.user.entity.Role;
import com.playdata.userservice.user.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.*;



public record UserSaveDto ( String username ,String password,String email,Role role){
}
