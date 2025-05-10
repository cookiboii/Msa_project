package com.playdata.orderservice.client;

import com.playdata.orderservice.common.dto.CommonResDto;
import com.playdata.orderservice.ordering.dto.ProductResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/product/{prodId}")
    CommonResDto<ProductResDto> findById(@PathVariable Long prodId);

    @PostMapping("/product/products")
    CommonResDto<List<ProductResDto>> getProducts(@RequestBody List<Long> productIds);

    @PutMapping("/product/cancel")
    ResponseEntity<?> cancelProduct(@RequestBody Map<Long, Integer> map);
}









