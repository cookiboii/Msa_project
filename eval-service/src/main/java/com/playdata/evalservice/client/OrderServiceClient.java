package com.playdata.evalservice.client;

import com.playdata.evalservice.common.auth.TokenUserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name= "order-service")
public interface OrderServiceClient {



}
