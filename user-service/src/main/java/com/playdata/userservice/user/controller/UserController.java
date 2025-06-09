package com.playdata.userservice.user.controller;

import com.playdata.userservice.common.auth.JwtTokenProvider;
import com.playdata.userservice.common.dto.CommonResDto;
import com.playdata.userservice.user.dto.*;
import com.playdata.userservice.user.dto.UserLoginDto;
import com.playdata.userservice.user.dto.UserPasswordUpdateDto;
import com.playdata.userservice.user.dto.UserResDto;
import com.playdata.userservice.user.dto.UserSaveDto;
import com.playdata.userservice.user.entity.User;
import com.playdata.userservice.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;

    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
         this.userService = userService;
         this.jwtTokenProvider = jwtTokenProvider;
     }
     @PostMapping("/create")
     public ResponseEntity<CommonResDto>   userSignIn( @RequestBody UserSaveDto userSaveDto) {

         User save = userService.Save(userSaveDto);
         CommonResDto resDto
                 = new CommonResDto(CREATED,
                 "User Created", save.getUsername());

         return new ResponseEntity<>(resDto, CREATED);
     }
    @PostMapping("/login")
    public ResponseEntity<CommonResDto> Login(@RequestBody  UserLoginDto  userLoginDto) {
            User user = userService.Login(userLoginDto);
        String token
                = jwtTokenProvider.createToken(user.getEmail(), user.getRole().toString());
        Map<String ,Object> loginInfo = new HashMap<>();
        loginInfo.put("token",token);
        loginInfo.put("id",user.getId());
        loginInfo.put("role", user.getRole().toString());

       CommonResDto resDto = new CommonResDto(OK,"Login Success",loginInfo);
        return new ResponseEntity<>(resDto, OK);

    }
/*   @DeleteMapping("/{id}")   //회원 탈퇴
    public void deleteUser(@PathVariable Long id) {
         userService.deleteUser(id);
   }*/

    @PostMapping("/password")
    public ResponseEntity<?> saveUser(@RequestBody UserPasswordUpdateDto updateDto) {
        User user = userService.updatePassword(updateDto);

        if(user == null) {
            return new ResponseEntity<>(FORBIDDEN);
        }

        return new ResponseEntity<CommonResDto>(ACCEPTED);
    }
    
    // 다른 서비스에서 feign 클라이언트로 요청을 보내는 로직임
    // 절대 삭제하지 말것!
    // 본인이 사용하지 않는 메소드라고 해서 생각없이 삭제하는 몰상식한 행동을 하지 않길 바람.
    @GetMapping("/findByEmail")
    public CommonResDto findByEmail(@RequestParam String email) {
        User foundUser = userService.findUserIdByEmail(email);

        UserResDto build = UserResDto.builder()
                .email(foundUser.getEmail())
                .id(foundUser.getId())
                .name(foundUser.getUsername())
                .role(foundUser.getRole())
                .build();

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "유저 찾음", build);
        return resDto;
    }
    @GetMapping("/myinfo")
    public ResponseEntity<CommonResDto> getUser() {
        User user = userService.usersearch();

        UserInfoResponseDto responseDto = UserInfoResponseDto.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .userId(user.getId())
                .build();

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "사용자 정보 조회 성공", responseDto);

        return new ResponseEntity<>(resDto, HttpStatus.OK);

    }

    // 카카오 콜백 요청 처리
    @GetMapping("/kakao")
    public void kakaoCallback(@RequestParam String code , HttpServletResponse response) throws IOException {
        log.info("카카오 콜백 처리 시작! code: {}", code);

        String kakaoAccessToken = userService.getKakaoAccessToken(code);

        KakaoUserDto kakaoUserDto =userService.getKakaoUser(kakaoAccessToken);
        UserResDto userResDto = userService.findOrCreateKakaoUser(kakaoUserDto);
        String token = jwtTokenProvider.createToken(userResDto.getEmail(),userResDto.getRole().toString());

        String html = String.format("""
                <!DOCTYPE html>
                <html>
                <head><title>카카오 로그인 완료</title></head>
                <body>
                    <script>
                        if (window.opener) {
                            window.opener.postMessage({
                                type: 'OAUTH_SUCCESS',
                                token: '%s',
                                id: '%s',
                                role: '%s',
                                provider: 'KAKAO'
                            }, 'http://localhost:5173');
                            window.close();
                        } else {
                            window.location.href = 'http://localhost:5173';
                        }
                    </script>
                    <p>카카오 로그인 처리 중...</p>
                </body>
                </html>
                """, token, userResDto.getId(), userResDto.getRole().toString());
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }

    @PostMapping("/email-valid")
    public  ResponseEntity <CommonResDto> emailValidate(@RequestBody Map<String,String> map    ) {
        String email = map.get("email");
        String authNum = userService.mailCheck(email);

        return ResponseEntity.ok().body(new CommonResDto(OK,"검증 완료 " ,authNum));
    }

}
