package com.playdata.postservice.post.service;

import com.playdata.postservice.client.ProductServiceClient;
import com.playdata.postservice.client.UserServiceClient;
import com.playdata.postservice.common.auth.TokenUserInfo;
import com.playdata.postservice.post.dto.postSaveReqDto;
import com.playdata.postservice.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    public void createPost(postSaveReqDto dto, TokenUserInfo userInfo) {

        String email = userInfo.getEmail();

        userServiceClient.getIdByEmail(email);

    }
}
