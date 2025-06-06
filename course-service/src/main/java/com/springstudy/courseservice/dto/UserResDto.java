package com.springstudy.courseservice.dto;


import com.springstudy.courseservice.common.auth.Role;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserResDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
}
