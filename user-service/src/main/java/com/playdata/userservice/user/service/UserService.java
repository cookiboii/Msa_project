package com.playdata.userservice.user.service;

import com.playdata.userservice.user.dto.UserLoginDto;
import com.playdata.userservice.user.dto.UserPasswordUpdateDto;
import com.playdata.userservice.user.dto.UserSaveDto;
import com.playdata.userservice.user.entity.User;
import com.playdata.userservice.user.repository.UserRepository;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
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
         String email = userSaveDto.getEmail();//회원가입
        String username = userSaveDto.getUsername();

         // 이메일 중복 체크
         if (userRepository.existsByEmail(email)) {
             throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
         }
        String password = userSaveDto.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        userSaveDto.setPassword(encodedPassword);
        userSaveDto.setUsername(username);
        userSaveDto.setEmail(userSaveDto.getEmail());

        User save = userRepository.save(userSaveDto.toEntity());


         return save;
    }

    @Transactional(readOnly = true)   //읽기 전용 트랜잭션
   public User search (String username) {  //  이름으로 이메일 찾기
        User user= userRepository.findByemail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));


 return user;
   }

   @Transactional
   public User updatePassword (UserPasswordUpdateDto updateDto) {
         //비밀번호변경
       User user  = userRepository.findById(updateDto.getId()).orElseThrow(() -> new UsernameNotFoundException("User not found: " + updateDto.getId()));
       String encodedPassword = passwordEncoder.encode(updateDto.getPassword());
       updateDto.setPassword(encodedPassword);


         return user;

   }


    public void deleteUser (Long id) {   //회원탈퇴
       userRepository.deleteUserById(id);
   }

    @Transactional
    public User Login (UserLoginDto userLoginDto) {
        User user = userRepository.findByemail(userLoginDto.getEmail() ).orElseThrow(() -> new EntityNotFoundException("User not found!"));

        if (!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호 틀렸습니다.");
        }

        return user;

     }

}
