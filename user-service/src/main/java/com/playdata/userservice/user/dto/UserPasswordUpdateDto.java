package com.playdata.userservice.user.dto;


import lombok.Builder;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

@Builder
public record UserPasswordUpdateDto ( @Email(message = "이메일 형식이 올바르지 않습니다.")
                                      @NotBlank(message = "이메일은 필수입니다.")
                                      String email,

                                      @NotBlank(message = "새 비밀번호는 필수입니다.")
                                      String newPassword) {

}
