package com.springstudy.courseservice.client;

import com.springstudy.courseservice.common.dto.CommonResDto;
import com.springstudy.courseservice.common.dto.ProdDetailResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "course-service")
public interface ProductServiceClient {

    @GetMapping("/courses/info/{id}")
    CommonResDto<ProdDetailResDto> findById(@PathVariable Long id);

    @PostMapping("/courses/products")
    CommonResDto<List<ProdDetailResDto>> getProducts(@RequestBody List<Long> productIds);

    @PostMapping("/courses/info/{id}")
    CommonResDto<List<ProdDetailResDto>> findById(@RequestBody List<Long> productIds);

    @PutMapping("/courses/delete")
    ResponseEntity<?> cancelProduct(@RequestBody Map<Long, Integer> map);
}









