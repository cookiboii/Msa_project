package com.playdata.userservice.user.service;

import com.playdata.userservice.user.dto.UserSaveDto;
import com.playdata.userservice.user.entity.User;
import com.playdata.userservice.user.repository.UserRepository;


import lombok.RequiredArgsConstructor;

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
    public User Save(UserSaveDto userSaveDto) {   //회원가입
        String username = userSaveDto.getUsername();
        String password = userSaveDto.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        userSaveDto.setPassword(encodedPassword);
        userSaveDto.setUsername(username);
        userSaveDto.setEmail(userSaveDto.getEmail());
        return userRepository.save(userSaveDto.toEntity());
    }

    @Transactional(readOnly = true)   //읽기 전용 트랜잭션
   public User search (String username) {
        User user= userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));


 return user;
   }

   public User updateUser (UserSaveDto userSaveDto) {



   }



}
