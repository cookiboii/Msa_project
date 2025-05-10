package com.playdata.orderservice.ordering.service;

import com.playdata.orderservice.client.UserServiceClient;
import com.playdata.orderservice.common.auth.Role;
import com.playdata.orderservice.common.auth.TokenUserInfo;
import com.playdata.orderservice.common.dto.CommonResDto;
import com.playdata.orderservice.ordering.dto.OrderingSaveReqDto;
import com.playdata.orderservice.ordering.dto.UserResDto;
import com.playdata.orderservice.ordering.entity.Ordering;
import com.playdata.orderservice.ordering.repository.OrderingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderingService {

    private final OrderingRepository orderingRepository;

    //feign client
    private final UserServiceClient userServiceClient;

    /*
    public List<Ordering> createOrder(TokenUserInfo userInfo, List<OrderingSaveReqDto> dtoList) {

        UserResDto userResDto;  //id, name, email, role
        Ordering ordering = new Ordering();
        List<Ordering> orderings = new ArrayList<>();

        log.info("구매요청고객정보: userInfo: {}, dtoList: {}", userInfo, dtoList);  //email, role

        //order객체 생성을 위한 고객 정보 얻어오기 -> user-service
        CommonResDto<UserResDto> byEmail
                = userServiceClient.findByEmail(userInfo.getEmail());

        userResDto = byEmail.getResult();
        log.info("user-service로부터 전달받은 결과: {}", userResDto);

        // Ordering(주문) 객체 생성
        for (OrderingSaveReqDto orderingSaveReqDto : dtoList) {

            ordering = Ordering.builder()
                    .userId(userResDto.getId())
                    .productId(orderingSaveReqDto.getProductId())  //리스트에 productId가 들어있음. ex)1,2
                    .build();

            orderings.add(ordering);
            orderingRepository.save(ordering);
        }

        return orderings;


    }
     */

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


}
