package com.playdata.postservice.client;


import com.playdata.postservice.common.dto.CommonResDto;
import com.playdata.postservice.post.dto.UserResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/user/findByEmail")
    CommonResDto<UserResDto> getIdByEmail(@RequestParam String email);

}
