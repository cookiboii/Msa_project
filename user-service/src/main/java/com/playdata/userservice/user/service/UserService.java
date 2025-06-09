package com.playdata.userservice.user.service;

import com.playdata.userservice.common.auth.TokenUserInfo;
import com.playdata.userservice.user.dto.*;
import com.playdata.userservice.user.entity.Role;
import com.playdata.userservice.user.entity.User;
import com.playdata.userservice.user.repository.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderService mailSenderService;
    @Value("${oauth2.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth2.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Transactional
    public User Save(UserSaveDto userSaveDto) {
        String email = userSaveDto.getEmail();
        String username = userSaveDto.getUsername();
        String password = userSaveDto.getPassword();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = User.builder()
                .username(username)
                .email(email)
                .password(encodedPassword)
                .role(userSaveDto.getRole())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User updatePassword(UserPasswordUpdateDto updateDto) {
        User user = userRepository.findByemail(updateDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + updateDto.getEmail()));

        if (passwordEncoder.matches(updateDto.getNewPassword(), user.getPassword())) {
            return null;
        }

        String encodedPassword = passwordEncoder.encode(updateDto.getNewPassword());
        user.changePassword(encodedPassword);
        return user;
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public User Login(UserLoginDto userLoginDto) {
        User user = userRepository.findByemail(userLoginDto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found!"));

        if (!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호 틀렸습니다.");
        }

        return user;
    }

    @Transactional(readOnly = true)
    public User usersearch() {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findByemail(userInfo.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found!"));

        return User.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .id(user.getId())
                .build();
    }

    public String getKakaoAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = "https://kauth.kakao.com/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("client_id", kakaoClientId);
        formData.add("redirect_uri", kakaoRedirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(requestUrl, HttpMethod.POST, request, Map.class);

        Map<String, Object> responseJSON = (Map<String, Object>) responseEntity.getBody();
        return (String) responseJSON.get("access_token");
    }

    public KakaoUserDto getKakaoUser(String kakaoAccessToken) {
        String requestUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer " + kakaoAccessToken);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<KakaoUserDto> response = restTemplate.exchange(
                requestUrl, HttpMethod.GET, new HttpEntity<>(headers), KakaoUserDto.class
        );

        return response.getBody();
    }
    @Transactional
    public UserResDto findOrCreateKakaoUser(KakaoUserDto kakaoUserDto) {
        Optional<User> existingUser = userRepository.findBySocialProviderAndSocialId("KAKAO", kakaoUserDto.getId().toString());

        if (existingUser.isPresent()) {
            return existingUser.get().toDto();
        } else {
            User newUser = User.builder()
                    .username(kakaoUserDto.getProperties().getNickname())
                    .email(kakaoUserDto.getKakaoAccount().getEmail())
                    .profileImage(kakaoUserDto.getProperties().getProfileImage())
                    .socialId(kakaoUserDto.getId().toString())
                    .socialProvider("KAKAO")
                    .password(null)
                    .build();

            return userRepository.save(newUser).toDto();
        }
    }

  public String mailCheck (String email) {
        Optional<User> byEmail = userRepository.findByemail(email);
        if (byEmail.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다!");
        }
      String authNum;
        try{
          authNum = mailSenderService.joinMain(email);
        }
        catch (MessagingException e){
            throw new RuntimeException("이메일 전송 과정 중 문제 발생!");
        }
        return authNum;
  }


}
