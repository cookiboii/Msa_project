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

    /*
        public List<Ordering> createOrder(TokenUserInfo userInfo, List<OrderingSaveReqDto> dtoList) {

            // UserResDto 객체를 직접 생성 (USER-SERVICE를 호출하지 않음)
            UserResDto userResDto = new UserResDto();
            userResDto.setId(1L);  // 임시 값 (실제 사용자 ID)
            userResDto.setName("김춘식");  // 임시 값 (실제 사용자 이름)
            userResDto.setEmail(userInfo.getEmail());  // 요청으로 받은 이메일
            userResDto.setRole(Role.USER);  // 임시 값 (사용자 역할)

            log.info("구매요청고객정보: userInfo: {}, dtoList: {}", userInfo, dtoList);

            // 주문 정보 생성
            List<Ordering> orderings = new ArrayList<>();
            for (OrderingSaveReqDto orderingSaveReqDto : dtoList) {
                Ordering ordering = Ordering.builder()
                        .userId(userResDto.getId())  // UserResDto의 ID 사용
                        .userEmail(userInfo.getEmail())
                        .productId(orderingSaveReqDto.getProductId())  // 각 dto에서 상품 ID 사용
                        .orderDate(LocalDate.now())
                        .build();

                orderings.add(ordering);
                orderingRepository.save(ordering);  // 주문 데이터 저장
            }

            return orderings;  // 생성된 주문 목록 반환
        }
    */

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

    public List<OrderingListResDto> myOrder(final TokenUserInfo userInfo) {
        String email = userInfo.getEmail();

        // UserResDto 객체를 직접 생성 (임시)
        UserResDto userDto = UserResDto.builder()
                .email(email)
                .role(Role.USER)
                .id(2L)
                .name("김춘식")
                .build();

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
//                            .userEmail(userDto.getEmail())
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

}