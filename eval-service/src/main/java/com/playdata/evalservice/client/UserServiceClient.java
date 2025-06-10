package com.playdata.evalservice.client;


import com.playdata.evalservice.common.dto.CommonResDto;
import com.playdata.evalservice.eval.dto.UserResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/user/findByEmail")
    CommonResDto<UserResDto> getIdByEmail(@RequestParam String email);

}
