package com.playdata.userservice.user.controller;

import com.playdata.userservice.common.auth.JwtTokenProvider;
import com.playdata.userservice.common.dto.CommonResDto;
import com.playdata.userservice.user.dto.UserLoginDto;
import com.playdata.userservice.user.dto.UserPasswordUpdateDto;
import com.playdata.userservice.user.dto.UserSaveDto;
import com.playdata.userservice.user.entity.User;
import com.playdata.userservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     public ResponseEntity<?>   userSignIn(@Valid @RequestBody UserSaveDto userSaveDto) {

         User save = userService.Save(userSaveDto);
         CommonResDto resDto
                 = new CommonResDto(HttpStatus.CREATED,
                 "User Created", save.getUsername());

         return new ResponseEntity<>(resDto, HttpStatus.CREATED);
     }
    @PostMapping("/login")
    public ResponseEntity<?> Login(@RequestBody  UserLoginDto  userLoginDto) {
            User user = userService.Login(userLoginDto);
       return new ResponseEntity<>(user, HttpStatus.OK);

    }
   @DeleteMapping("/{id}")   //회원 탈퇴
    public void deleteUser(@PathVariable Long id) {
         userService.deleteUser(id);
   }
   @GetMapping("/username")   //이메일로 이름찾기
    public ResponseEntity<?> getUsers(UserSaveDto userSaveDto) {
     User user  =  userService.search(userSaveDto.getUsername());

     return new ResponseEntity<>(user, HttpStatus.OK);
   }
    @PutMapping("/password")
    public ResponseEntity<?> saveUser(@RequestBody UserPasswordUpdateDto updateDto) {
        User user = userService.updatePassword(updateDto);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
