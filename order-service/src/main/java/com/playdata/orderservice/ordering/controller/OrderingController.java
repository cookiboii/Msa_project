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


    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal TokenUserInfo userInfo,
                                         @RequestBody List<OrderingSaveReqDto> dtoList) {

        //유저정보, 주문요청데이터 받아옴
        System.out.println("createOrder 들어옴");
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


    // 내 주문만 볼 수 있는 MyOrders
    @GetMapping("/my-order")
    public ResponseEntity<?> myOrder(
            @AuthenticationPrincipal TokenUserInfo userInfo) {
        List<OrderingListResDto> dtos = orderingService.myOrder(userInfo);
        CommonResDto<List<OrderingListResDto>> resDto =
                new CommonResDto<>(HttpStatus.OK, "정상 조회 완료", dtos);
        return ResponseEntity.ok(resDto);
    }


    // 강사용 본인 강의 주문 내역 조회
    @GetMapping("/my-course-order")
    public ResponseEntity<?> getAllOrders(@AuthenticationPrincipal TokenUserInfo userInfo){

//        if (!userInfo.getRole().equals(Role.ADMIN)) {
//            throw new AccessDeniedException("관리자만 접근 가능합니다.");
//        }
        List<OrderingListResDto> dtos = orderingService.myCourseOrder(userInfo);
        CommonResDto<List<OrderingListResDto>> resDto =
                new CommonResDto<>(HttpStatus.OK, "전체 주문 내역 조회 완료", dtos);
        return ResponseEntity.ok(resDto);
    }


}
