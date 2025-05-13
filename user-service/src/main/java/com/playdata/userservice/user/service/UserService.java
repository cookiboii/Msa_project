package com.playdata.userservice.user.service;

import com.playdata.userservice.common.auth.TokenUserInfo;
import com.playdata.userservice.user.dto.*;
import com.playdata.userservice.user.entity.Role;
import com.playdata.userservice.user.entity.User;
import com.playdata.userservice.user.repository.UserRepository;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserService {

      private final UserRepository userRepository;


    private final PasswordEncoder passwordEncoder;



    @Transactional
    public User Save(UserSaveDto userSaveDto) {
        String email = userSaveDto.getEmail();
        String username = userSaveDto.getUsername();
        String password = userSaveDto.getPassword();

        // 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        String encodedPassword = passwordEncoder.encode(password);
        User user  = User.builder()
                        .username(username)
                       .email(email)
                     .password(encodedPassword)
                             .role(userSaveDto.getRole())
                .build();

        return userRepository.save(user);
    }



   @Transactional
   public User updatePassword (UserPasswordUpdateDto updateDto) {
         //비밀번호변경
       User user  = userRepository.findById(updateDto.getId()).orElseThrow(() -> new UsernameNotFoundException("User not found: " + updateDto.getId()));
       String encodedPassword = passwordEncoder.encode(updateDto.getPassword());
       user.changePassword(encodedPassword);


         return user;

   }

   @Transactional
    public void deleteUser (Long id) {   //회원탈퇴
       userRepository.deleteById(id);
   }

    @Transactional
    public User Login (UserLoginDto userLoginDto) {   //로그인
        User user = userRepository.findByemail(userLoginDto.getEmail() ).orElseThrow(() -> new EntityNotFoundException("User not found!"));

        if (!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호 틀렸습니다.");
        }

        return user;

     }







}
