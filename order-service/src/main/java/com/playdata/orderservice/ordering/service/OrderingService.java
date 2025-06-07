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
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderingService {
    @Value("${oauth2.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth2.kakao.redirect_uri}")
    private String kakaoRedirectUri;

    @Value("${oauth2.kakao.secret_key}")
    private String secretKey;

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
    @Transactional(readOnly = true)
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
                            .userEmail(email)
                            .userId(userDto.getId())
                            .productId(ordering.getProductId())
                            .productName(product != null ? product.getProductName() : "Unknown")
                            .orderStatus(ordering.getOrderStatus())
                            .orderDate(ordering.getOrderDate())
                            .category(product != null ? product.getCategory() : null)
                            .filePath(product != null ? product.getFilePath() : null)
                            .active(product != null ? product.isActive() : null)
                            .build();
                })
                .collect(Collectors.toList());


    }

    // 모든 주문 정보 리턴
    @Transactional(readOnly = true)
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
                            .userEmail(ordering.getUserEmail())
                            .productId(ordering.getProductId())
                            .productName(product != null ? product.getProductName() : "Unknown")
                            .orderStatus(ordering.getOrderStatus())
                            .orderDate(ordering.getOrderDate())
                            .category(product != null ? product.getCategory() : null)
                            .filePath(product != null ? product.getFilePath() : null)
                            .active(product != null ? product.isActive() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 강사의 본인 강의 주문 내역 리턴
    @Transactional(readOnly = true)
    public List<OrderingListResDto> myCourseOrder(final TokenUserInfo userInfo) {
        String email = userInfo.getEmail();

        // 이메일로는 주문 회원 정보를 알 수가 없음. (id로 되어 있으니까)
        CommonResDto<UserResDto> byEmail
                = userServiceClient.findByEmail(email);
        UserResDto userDto = byEmail.getResult();

        System.out.println("userId = " + userDto.getId());
        Map<String, Long> request = new HashMap<>();
        request.put("userId", userDto.getId());

        // 강사가 등록한 강의 목록 조회
        CommonResDto<List<ProdDetailResDto>> courseRes = productServiceClient.getProductsByUserId(request);
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
                            .userEmail(ordering.getUserEmail())
                            .userId(ordering.getUserId())
                            .productId(ordering.getProductId())
                            .productName(course != null ? course.getProductName() : "Unknown")
                            .orderStatus(ordering.getOrderStatus())
                            .orderDate(ordering.getOrderDate())
                            .category(course != null ? course.getCategory() : null)
                            .filePath(course != null ? course.getFilePath() : null)
                            .active(course != null ? course.isActive() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 수강 가능한 강의 목록 리턴
    @Transactional(readOnly = true)
    public List<OrderingListResDto> myDashboard(final TokenUserInfo userInfo) {
        // 전체 주문 목록을 먼저 가져옴
        List<OrderingListResDto> allOrders = myOrder(userInfo);

        log.info("allOrders : {}", allOrders);

        // 그 중에서 주문 상태가 ORDERED이고, active가 true인 주문만 필터링
        return allOrders.stream()
                .peek(order -> System.out.println("필터 통과전 orderId: " + order.getId()))
                .filter(order ->
                        order.getOrderStatus() == OrderStatus.ORDERED &&
                                order.isActive()
                )
                .peek(order -> System.out.println("필터 통과한 orderId: " + order.getId()))
                .collect(Collectors.toList());
    }

    // 카카오페이 결제창 연결
    public KakaoPayDTO payReady(TokenUserInfo userInfo, List<OrderingSaveReqDto> dtoList) {

        log.info("service 내 payReady<dtoList> : {}", dtoList);

        // 실제 USER-SERVICE에서 사용자 정보 요청
        UserResDto userResDto = userServiceClient.findByEmail(userInfo.getEmail()).getResult();

//        List<Long> productIds = orderingList.stream()
//                .map(Ordering::getProductId)
//                .distinct()
//                .collect(Collectors.toList());
//
//        log.info("productIds는 : {}", productIds);
//
//        // 외부 서비스에서 한번에 강의 정보 목록 가져오기
//        CommonResDto<List<ProdDetailResDto>> courseRes = productServiceClient.getProducts(dtoList);
//        List<ProdDetailResDto> dtoList = courseRes.getResult();
        log.info("본문 작성 시작");

        //요청 본문 작성
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("cid", "TC0ONETIME");
        requestBody.add("partner_order_id", "1234567890");
        requestBody.add("partner_user_id", userResDto.getEmail());
        requestBody.add("item_name", "스프링 기본");
        requestBody.add("quantity", "1");
        requestBody.add("total_amount", "1000");
        requestBody.add("tax_free_amount", "0");
        requestBody.add("approval_url", "http://localhost/order-service/order/pay/completed");
        requestBody.add("cancel_url", "http://localhost/order-service/order/pay/cancel");
        requestBody.add("fail_url", "http://localhost/order-service/order/pay/fail");

        // HttpEntity : HTTP 요청 또는 응답에 해당하는 Http Header와 Http Body를 포함하는 클래스
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, this.getHeaders());
        log.info("restTemplate 시작");
        // RestTemplate
        // : Rest 방식 API를 호출할 수 있는 Spring 내장 클래스
        //   REST API 호출 이후 응답을 받을 때까지 기다리는 동기 방식 (json, xml 응답)
        RestTemplate template = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/ready";
        // RestTemplate의 postForEntity : POST 요청을 보내고 ResponseEntity로 결과를 반환받는 메소드
        ResponseEntity<KakaoPayDTO> responseEntity = template.postForEntity(url, requestEntity, KakaoPayDTO.class);
        log.info("결제준비 응답객체: " + responseEntity.getBody());

        return responseEntity.getBody();
    }

    // 카카오페이 결제 승인
    // 사용자가 결제 수단을 선택하고 비밀번호를 입력해 결제 인증을 완료한 뒤,
    // 최종적으로 결제 완료 처리를 하는 단계
    public KakaoPayAproveResponse payApprove(String tid, String pgToken) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", "TC0ONETIME");              // 가맹점 코드(테스트용)
        parameters.put("tid", tid);                       // 결제 고유번호
        parameters.put("partner_order_id", "1234567890"); // 주문번호
        parameters.put("partner_user_id", "roommake");    // 회원 아이디
        parameters.put("pg_token", pgToken);              // 결제승인 요청을 인증하는 토큰

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(parameters, this.getHeaders());

        RestTemplate template = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/approve";
        KakaoPayAproveResponse approveResponse = template.postForObject(url, requestEntity, KakaoPayAproveResponse.class);
        log.info("결제승인 응답객체: " + approveResponse);

        return approveResponse;
    }

    // 카카오페이 측에 요청 시 헤더부에 필요한 값
    private HttpHeaders getHeaders() {

        // 헤더 정보 세팅Add commentMore actions
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "SECRET_KEY " + secretKey);

        return headers;
    }


}