package com.playdata.userservice.user.dto;


import com.playdata.userservice.user.entity.Role;
import com.playdata.userservice.user.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSaveDto {


   private String username;

   @NotNull
   private String password;
   @NotNull
   private String email;

   public User toEntity() {
      return  User.builder()
              .email(this.email)
              .password(this.password)  //비밀번호 해쉬화
              .username(this.username)
              .role(Role.USER)
              .build();
   }

}
