package com.playdata.orderservice.ordering.service;

import com.playdata.orderservice.client.ProductServiceClient;
import com.playdata.orderservice.client.UserServiceClient;
import com.playdata.orderservice.common.auth.Role;
import com.playdata.orderservice.common.auth.TokenUserInfo;
import com.playdata.orderservice.common.dto.CommonResDto;
import com.playdata.orderservice.ordering.dto.*;
import com.playdata.orderservice.ordering.entity.OrderStatus;
import com.playdata.orderservice.ordering.entity.Ordering;
import com.playdata.orderservice.ordering.repository.OrderingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderingService {

    private final OrderingRepository orderingRepository;

    //feign client
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;

    // 주문 생성
    public List<Ordering> createOrder(TokenUserInfo userInfo, List<OrderingSaveReqDto> dtoList) {

        // 실제 USER-SERVICE에서 사용자 정보 요청
        UserResDto userResDto = userServiceClient.findByEmail(userInfo.getEmail()).getResult();


        log.info("구매요청고객정보: userInfo: {}, dtoList: {}", userInfo, dtoList);

        List<Ordering> orderings = new ArrayList<>();
        for (OrderingSaveReqDto orderingSaveReqDto : dtoList) {
            Ordering ordering = Ordering.builder()
                    .userId(userResDto.getId())
                    .userEmail(userResDto.getEmail())
                    .productId(orderingSaveReqDto.getProductId())
                    .orderDate(LocalDate.now())
                    .build();

            orderings.add(ordering);
            orderingRepository.save(ordering);
        }

        return orderings;
    }

    // 주문 취소
    public Ordering cancelOrder(long id) {
        Ordering ordering = orderingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("주문 없음")
        );
        // 주문 entity의 status를 CANCELED로 변경
//        List<OrderDetail> orderDetailList = ordering.getOrderDetails();

        ordering.updateStatus(OrderStatus.CANCELED);
//        orderingRepository.save(ordering);
        return ordering;
    }

    // 나의 주문 정보 리턴
    public List<OrderingListResDto> myOrder(final TokenUserInfo userInfo) {
        String email = userInfo.getEmail();

        // 이메일로는 주문 회원 정보를 알 수가 없음. (id로 되어 있으니까)
        CommonResDto<UserResDto> byEmail
                = userServiceClient.findByEmail(email);
        UserResDto userDto = byEmail.getResult();

        // 실제 USER-SERVICE에서 사용자 정보 요청
//        UserResDto userResDto = userServiceClient.findByEmail(userInfo.getEmail()).getResult();

        List<Ordering> orderingList
                = orderingRepository.findByUserId(userDto.getId());

        List<Long> productIds = orderingList.stream()
                .map(Ordering::getProductId)
                .distinct()
                .collect(Collectors.toList());

        log.info("productIds는 : {}", productIds);

        // 외부 서비스에서 한번에 강의 정보 목록 가져오기
        CommonResDto<List<ProdDetailResDto>> courseRes = productServiceClient.getProducts(productIds);
        List<ProdDetailResDto> dtoList = courseRes.getResult();

        log.info("dtolist는 : {}", dtoList);

        // dtoList → Map<productId, ProdDetailResDto>
        Map<Long, ProdDetailResDto> productMap = dtoList.stream()
                .collect(Collectors.toMap(ProdDetailResDto::getProductId, dto -> dto));

        return orderingList.stream()
                .map(ordering -> {
                    ProdDetailResDto product = productMap.get(ordering.getProductId());

                    return OrderingListResDto.builder()
                            .id(ordering.getId())
                            .userEmail(userDto.getEmail())
                            .productId(ordering.getProductId())
                            .productName(product != null ? product.getProductName() : "Unknown")
                            .orderStatus(ordering.getOrderStatus())
                            .orderDate(ordering.getOrderDate())
                            .category(product != null ? product.getCategory() : null)
                            .filePath(product != null ? product.getFilePath() : null)
                            .build();
                })
                .collect(Collectors.toList());


    }

    // 모든 주문 정보 리턴
    public List<OrderingListResDto> findAllOrders() {
        List<Ordering> orderList = orderingRepository.findAll();

        List<Long> productIds = orderList.stream()
                .map(Ordering::getProductId)
                .distinct()
                .toList();

        log.info("productIds는 : {}", productIds);

        // 외부 서비스에서 한번에 강의 정보 목록 가져오기
        CommonResDto<List<ProdDetailResDto>> courseRes = productServiceClient.getProducts(productIds);
        List<ProdDetailResDto> dtoList = courseRes.getResult();

        log.info("dtolist는 : {}", dtoList);

        // dtoList → Map<productId, ProdDetailResDto>
        Map<Long, ProdDetailResDto> productMap = dtoList.stream()
                .collect(Collectors.toMap(ProdDetailResDto::getProductId, dto -> dto));


        return orderList.stream()
                .map(ordering -> {
                    ProdDetailResDto product = productMap.get(ordering.getProductId());

                    return OrderingListResDto.builder()
                            .id(ordering.getId())
                            .userId(ordering.getUserId())
                            .productId(ordering.getProductId())
                            .productName(product != null ? product.getProductName() : "Unknown")
                            .orderStatus(ordering.getOrderStatus())
                            .orderDate(ordering.getOrderDate())
                            .category(product != null ? product.getCategory() : null)
                            .filePath(product != null ? product.getFilePath() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 나의 강의 주문 내역 리턴
    public List<OrderingListResDto> myCourseOrder(final Long userId) {

        System.out.println("userId = " + userId);

        // 강사가 등록한 강의 목록 조회
        CommonResDto<List<ProdDetailResDto>> courseRes = productServiceClient.getProductsByUserId(userId);
        List<ProdDetailResDto> myCourses = courseRes.getResult();

        List<Long> myProductIds = myCourses.stream()
                .map(ProdDetailResDto::getProductId)
                .collect(Collectors.toList());

        if (myProductIds.isEmpty()) {
            return Collections.emptyList(); // 강사가 등록한 강의가 없다면 빈 리스트 반환
        }

        // Ordering 테이블에서 강사의 강의에 대한 주문 정보들 조회
        List<Ordering> orderingList = orderingRepository.findByProductIdIn(myProductIds);

        // 4. productId -> ProdDetailResDto 매핑
        Map<Long, ProdDetailResDto> courseMap = myCourses.stream()
                .collect(Collectors.toMap(ProdDetailResDto::getProductId, c -> c));

        // DTO 리턴
        return orderingList.stream()
                .map(ordering -> {
                    ProdDetailResDto course = courseMap.get(ordering.getProductId());

                    return OrderingListResDto.builder()
                            .id(ordering.getId())
//                            .userEmail("unknown")
                            .productId(ordering.getProductId())
                            .productName(course != null ? course.getProductName() : "Unknown")
                            .orderStatus(ordering.getOrderStatus())
                            .orderDate(ordering.getOrderDate())
                            .category(course != null ? course.getCategory() : null)
                            .filePath(course != null ? course.getFilePath() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }


}