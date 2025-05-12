package com.playdata.orderservice.ordering.controller;

import com.playdata.orderservice.common.auth.Role;
import com.playdata.orderservice.common.auth.TokenUserInfo;
import com.playdata.orderservice.common.dto.CommonResDto;
import com.playdata.orderservice.common.dto.CreateOrderRequest;
import com.playdata.orderservice.ordering.dto.OrderingListResDto;
import com.playdata.orderservice.ordering.dto.OrderingSaveReqDto;
import com.playdata.orderservice.ordering.entity.Ordering;
import com.playdata.orderservice.ordering.service.OrderingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderingController {

    private final OrderingService orderingService;

/*
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {

        // 임시 사용자 정보 생성
        TokenUserInfo userInfo = TokenUserInfo.builder()
                .email(request.getEmail())
                .role(Role.USER) // 적절한 값 넣기
                .build();

        log.info("/order/create: POST 요청, userInfo: {}", userInfo);
        log.info("dtoList: {}", request.getDtoList());

        List<Ordering> orderings = orderingService.createOrder(userInfo, request.getDtoList());

        CommonResDto resDto = new CommonResDto<>(HttpStatus.CREATED, "정상 주문 완료", orderings);

        return new ResponseEntity<>(resDto, HttpStatus.CREATED);
    }

 */


    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal TokenUserInfo userInfo,
                                         @RequestBody List<OrderingSaveReqDto> dtoList) {

        //유저정보, 주문요청데이터 받아옴
        log.info("/order/create: POST 요청, userInfo: {}", userInfo);
        log.info("dtoList: {}", dtoList);

        List<Ordering> orderings = orderingService.createOrder(userInfo, dtoList);

        CommonResDto resDto = new CommonResDto<>(HttpStatus.CREATED, "정상 주문 완료", orderings);

        return new ResponseEntity<>(resDto, HttpStatus.CREATED);

    }


    // 주문 상태를 취소로 변경하는 요청
    @PatchMapping("/cancel/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable long id) {
        Ordering ordering = orderingService.cancelOrder(id);
        CommonResDto resDto
                = new CommonResDto(HttpStatus.OK, "주문 취소 완료", ordering.getId());
        return ResponseEntity.ok().body(resDto);
    }

    // 내 주문 조회 요청
    @GetMapping("/my-order")
    public ResponseEntity<?> myOrder(@RequestParam String email) {
        TokenUserInfo userInfo = TokenUserInfo.builder()
                .email(email)
                .role(Role.USER)
                .build();

        List<OrderingListResDto> dtos = orderingService.myOrder(userInfo);
        CommonResDto<List<OrderingListResDto>> resDto =
                new CommonResDto<>(HttpStatus.OK, "정상 조회 완료", dtos);
        return ResponseEntity.ok(resDto);
    }

/*
    @GetMapping("/admin/all-orders")
    @PreAuthorize("hasRole('ADMIN')") // Spring Security 사용 시 관리자 권한 제한
    public ResponseEntity<?> getAllOrders() {
        List<OrderingListResDto> dtos = orderingService.findAllOrders();
        CommonResDto<List<OrderingListResDto>> resDto =
                new CommonResDto<>(HttpStatus.OK, "전체 주문 내역 조회 완료", dtos);
        return ResponseEntity.ok(resDto);
    }
*/

    // 관리자용 전체 주문 내역 조회
    @GetMapping("/admin/all-orders")
    public ResponseEntity<?> getAllOrders(@AuthenticationPrincipal TokenUserInfo userInfo) throws AccessDeniedException {

        TokenUserInfo user = TokenUserInfo.builder()
                .email("pa@naver.com")
                .role(Role.ADMIN)
                .build();

        if (!user.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("관리자만 접근 가능합니다.");
        }
        List<OrderingListResDto> dtos = orderingService.findAllOrders();
        CommonResDto<List<OrderingListResDto>> resDto =
                new CommonResDto<>(HttpStatus.OK, "전체 주문 내역 조회 완료", dtos);
        return ResponseEntity.ok(resDto);
    }


}
