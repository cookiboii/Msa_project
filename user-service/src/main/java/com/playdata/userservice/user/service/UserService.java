package com.playdata.userservice.user.service;

import com.playdata.userservice.common.auth.JwtTokenProvider;
import com.playdata.userservice.common.auth.TokenUserInfo;
import com.playdata.userservice.user.dto.*;
import com.playdata.userservice.user.entity.Role;
import com.playdata.userservice.user.entity.User;
import com.playdata.userservice.user.repository.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderService mailSenderService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String VERIFICATION_CODE_KEY = "email_verify:code:";
    private static final String VERIFICATION_ATTEMPT_KEY = "email_verify:attempt:";
    private static final String VERIFICATION_BLOCK_KEY = "email_verify:block:";

    @Value("${oauth2.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth2.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Transactional
    public User Save(UserSaveDto userSaveDto) {
        String email = userSaveDto.email();
        String username = userSaveDto.username();
        String password = userSaveDto.password();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = User.builder()
                .username(username)
                .email(email)
                .password(encodedPassword)
                .role(userSaveDto.role())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User updatePassword(UserPasswordUpdateDto updateDto) {
        User user = userRepository.findByEmail(updateDto.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + updateDto.email()));

        if (passwordEncoder.matches(updateDto.email(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        String encodedPassword = passwordEncoder.encode(updateDto.newPassword());
        user.changePassword(encodedPassword);
        return user;
    }


    @Transactional
    public User Login(UserLoginDto userLoginDto) {
        String email = userLoginDto.email();
        String inputPassword = userLoginDto.password();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found!"));

        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        return user;
    }

    @Transactional(readOnly = true)
    public User usersearch() {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findByEmail(userInfo.getEmail())
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
        Optional<User> byEmail = userRepository.findByEmail(email);
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
      String key = VERIFICATION_CODE_KEY + email;
      redisTemplate.opsForValue().set(key, authNum, Duration.ofMinutes(1));
        return authNum;
  }
    @Transactional
    public String resetPassword(String email) {
        log.info(email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found! " + email));

        String tempPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String encodedPassword = passwordEncoder.encode(tempPassword);
        user.changePassword(encodedPassword);

        try {
            mailSenderService.sendTempPasswordMail(email, tempPassword);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 실패");
        }

        return "임시비밀번호가 전송되었습니다.";
    }
    // 인증 코드 검증 로직
    public Map<String, String> verifyEmail(Map<String, String> map) {
        // 차단 상태 확인
       /* if (isBlocked(map.get("email"))) {
            throw new IllegalArgumentException("blocking");
        }*/

        // 레디스에 저장된 인증 코드 조회
        String key = VERIFICATION_CODE_KEY + map.get("email");
        Object foundCode = redisTemplate.opsForValue().get(key);
        if (foundCode == null) { // 조회결과가 null? -> 만료됨!
            throw new IllegalArgumentException("authCode expired!");
        }

        // 인증 시도 횟수 증가
        int attemptCount = incrementAttemptCount(map.get("email"));

        // 조회한 코드와 사용자가 입력한 인증번호 검증
        if (!foundCode.toString().equals(map.get("code"))) {
            // 최대 시도 횟수 초과시 차단
            if (attemptCount >= 1000) {
                blockUser(map.get("email"));
                throw new IllegalArgumentException("email blocked!");
            }
            int remainingAttempts = 100000 - attemptCount;
            throw new IllegalArgumentException(String.format("authCode wrong!, %d", remainingAttempts));
        }

        log.info("이메일 인증 성공!, email: {}", map.get("email"));
        redisTemplate.delete(key); // 레디스에서 인증번호 삭제
        return map;
    }

    private boolean isBlocked(String email) {
        String key = VERIFICATION_BLOCK_KEY + email;
        return redisTemplate.hasKey(key);
    }

    private void blockUser(String email) {
        String key = VERIFICATION_BLOCK_KEY + email;
        redisTemplate.opsForValue().set(key, "blocked", Duration.ofMinutes(30));
    }

    private int incrementAttemptCount(String email) {
        String key = VERIFICATION_ATTEMPT_KEY + email;
        Object obj = redisTemplate.opsForValue().get(key);

        int count = (obj != null) ? Integer.parseInt(obj.toString()) + 1 : 1;
        redisTemplate.opsForValue().set(key, String.valueOf(count), Duration.ofMinutes(3));

        return count;
    }
    @Transactional
    public void sendResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다."));
        String code = String.valueOf((int) ((Math.random() * 900000) + 100000)); // 6자리 숫자
        redisTemplate.opsForValue().set("reset:" + email, code, Duration.ofMinutes(5));
        try {
            mailSenderService.sendAuthCode(email, code);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송 실패");
        }
    }
    public boolean verifyResetCode(String email, String inputCode) {
        String redisKey = "reset:" + email;
        String savedCode = redisTemplate.opsForValue().get(redisKey);
        return savedCode != null && savedCode.equals(inputCode);
    }

    @Transactional
    public void updatePasswordAfterVerification(UserPasswordUpdateDto updateDto) {
        String email = updateDto.email();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다."));

        String encodedPassword = passwordEncoder.encode(updateDto.newPassword());
        user.changePassword(encodedPassword);
        redisTemplate.delete("reset:" + email); // 인증 코드 삭제
    }

}
