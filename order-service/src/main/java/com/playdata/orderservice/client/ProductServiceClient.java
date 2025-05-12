package com.playdata.orderservice.client;

import com.playdata.orderservice.common.dto.CommonResDto;
import com.playdata.orderservice.ordering.dto.ProdDetailResDto;
import com.playdata.orderservice.ordering.dto.ProductResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "course-service")
public interface ProductServiceClient {

    @GetMapping("/courses/info/{prodId}")
    CommonResDto<ProdDetailResDto> findById(@PathVariable Long prodId);

    @PostMapping("/course/products")
    CommonResDto<List<ProductResDto>> getProducts(@RequestBody List<Long> productIds);

    @PutMapping("/course/cancel")
    ResponseEntity<?> cancelProduct(@RequestBody Map<Long, Integer> map);
}









