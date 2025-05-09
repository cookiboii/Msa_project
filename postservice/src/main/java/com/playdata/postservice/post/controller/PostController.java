package com.playdata.postservice.post.controller;

import com.playdata.postservice.common.auth.TokenUserInfo;
import com.playdata.postservice.post.dto.postSaveReqDto;
import com.playdata.postservice.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<?> createPost(@AuthenticationPrincipal TokenUserInfo userInfo,
            @RequestBody postSaveReqDto dto) {

        postService.createPost(dto, userInfo);
    }

}
