package com.playdata.userservice.user.dto;


import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordUpdateDto {

    private Long Id;
    private String Password;
}
