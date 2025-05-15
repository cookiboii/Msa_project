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
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/password")
    public ResponseEntity<?> saveUser(@RequestBody UserPasswordUpdateDto updateDto) {
        User user = userService.updatePassword(updateDto);

        if(user == null) {
            return new ResponseEntity<>(FORBIDDEN);
        }

        return new ResponseEntity<CommonResDto>(ACCEPTED);
    }

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


}
