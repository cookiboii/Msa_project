package com.playdata.userservice.user.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "user_tb")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String username;


    private String password;

    private String email;


    @Enumerated(EnumType.STRING)
    private Role role;

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
    //이렇게 구현한 이유는 세터는 필요할떄 꺼내는쓰는게 좋다
    //@Setter
}
