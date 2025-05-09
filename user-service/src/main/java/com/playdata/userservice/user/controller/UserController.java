package com.playdata.userservice.user.controller;

import com.playdata.userservice.user.service.UserService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

     private final UserService userService;

     public UserController(UserService userService) {
         this.userService = userService;
     }

}
