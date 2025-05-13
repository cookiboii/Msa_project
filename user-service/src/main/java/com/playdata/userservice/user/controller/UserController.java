package com.playdata.userservice.user.controller;

import com.playdata.userservice.common.auth.JwtTokenProvider;
import com.playdata.userservice.common.auth.TokenUserInfo;
import com.playdata.userservice.common.dto.CommonResDto;
import com.playdata.userservice.user.dto.*;
import com.playdata.userservice.user.entity.User;
import com.playdata.userservice.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/password")
    public ResponseEntity<CommonResDto> saveUser(@RequestBody UserPasswordUpdateDto updateDto) {
        User user = userService.updatePassword(updateDto);
        CommonResDto resDto = new CommonResDto(HttpStatus.OK, "Password", user.getUsername());
        return new ResponseEntity<CommonResDto>(OK);
    }

    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal TokenUserInfo userInfo ) {
        // userInfo에서 사용자 email을 꺼내서 사용자 정보
        String email = userInfo.getEmail();
        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "myInfo 조회 성공", email );


        return new ResponseEntity<>(resDto , HttpStatus.OK);
    }

}
