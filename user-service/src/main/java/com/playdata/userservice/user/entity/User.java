package com.playdata.userservice.user.entity;


import com.playdata.userservice.common.entity.BaseTimeEntity;
import com.playdata.userservice.user.dto.UserInfoDto;
import com.playdata.userservice.user.dto.UserSaveDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "user_tb")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String username;


    private String password;

    private String email;


    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }




    //이렇게 구현한 이유는 세터는 필요할떄 꺼내는쓰는게 좋다
      public UserSaveDto toEntity() {
          return UserSaveDto.builder()
                          .username(this.username)
                                  .email(this.email)
                  .password(this.password)
                  .role(this.role)
                  .build();
      }

}
