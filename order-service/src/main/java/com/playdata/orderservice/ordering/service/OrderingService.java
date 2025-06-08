package com.playdata.orderservice.ordering.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playdata.orderservice.client.ProductServiceClient;
import com.playdata.orderservice.client.UserServiceClient;
import com.playdata.orderservice.common.auth.Role;
import com.playdata.orderservice.common.auth.TokenUserInfo;
import com.playdata.orderservice.common.dto.CommonResDto;
import com.playdata.orderservice.ordering.controller.SessionUtils;
import com.playdata.orderservice.ordering.dto.*;
import com.playdata.orderservice.ordering.entity.OrderStatus;
import com.playdata.orderservice.ordering.entity.Ordering;
import com.playdata.orderservice.ordering.entity.Payment;
import com.playdata.orderservice.ordering.repository.OrderingRepository;
import com.playdata.orderservice.ordering.repository.PaymentRepository;
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
import java.time.LocalDateTime;
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
    private final PaymentRepository paymentRepository;

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
    @Transactional
    public KakaoPayDTO payReady(TokenUserInfo userInfo, List<OrderingSaveReqDto> dtoList) {

        log.info("service 내 payReady <dtoList> : {}", dtoList);

        // 실제 USER-SERVICE에서 사용자 정보 요청
        UserResDto userResDto = userServiceClient.findByEmail(userInfo.getEmail()).getResult();

        List<Ordering> createdOrders = createOrder(userInfo, dtoList);
        // 결제와 연결할 대표 주문 엔티티 선택 (여기서는 첫 번째 주문을 대표로 사용)
        Ordering representativeOrder = createdOrders.get(0);
        String partnerOrderId = UUID.randomUUID().toString(); // 카카오페이에 넘길 고유 주문 번호 (UUID)

        // dtoList에서 productId 목록 추출
        List<Long> productIds = dtoList.stream()
                .map(OrderingSaveReqDto::getProductId)
                .collect(Collectors.toList());

        log.info("productIds는 : {}", productIds);

        // 외부 서비스에서 한번에 강의 정보 목록 가져오기
        CommonResDto<List<ProdDetailResDto>> courseRes = productServiceClient.getProducts(productIds);
        List<ProdDetailResDto> productDetails = courseRes.getResult();

        // 각 상품의 가격을 합산하여 total_amount 계산
        Integer totalAmount = 100;
        for (OrderingSaveReqDto orderDto : dtoList) {
            // dtoList의 각 상품과, productDetails에서 해당 상품의 가격을 찾아서 합산
            ProdDetailResDto matchedProduct = productDetails.stream()
                    .filter(p -> p.getProductId().equals(orderDto.getProductId())) // ProdDetailResDto에 getProductId() 필요
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("주문된 상품에 대한 가격 정보를 찾을 수 없습니다: " + orderDto.getProductId()));

            log.info("matchedProduct : {}", matchedProduct);

            totalAmount += matchedProduct.getPrice();
        }
        log.info("총 결제 금액: {}", totalAmount);

        String itemName;
        if (productDetails.size() == 1) {
            // 상품이 하나일 경우, 해당 상품의 이름만 표시
            itemName = productDetails.get(0).getProductName();
        } else {
            // 상품이 두 개 이상일 경우, 첫 번째 상품 이름 + " 외 N건"
            itemName = productDetails.get(0).getProductName() + " 외 " + (productDetails.size() - 1) + "건";
        }

        log.info("본문 작성 시작");
        //요청 본문 작성
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("cid", "TC0ONETIME");
        requestBodyMap.put("partner_order_id", partnerOrderId);
        requestBodyMap.put("partner_user_id", userResDto.getEmail());
        requestBodyMap.put("item_name", itemName);
        requestBodyMap.put("quantity", 1);
        requestBodyMap.put("total_amount", totalAmount);
        requestBodyMap.put("tax_free_amount", 0);
        requestBodyMap.put("approval_url", "http://localhost:8000/order-service/order/pay/completed?orderId=" + partnerOrderId);
        requestBodyMap.put("cancel_url", "http://localhost:8000/order-service/order/pay/cancel");
        requestBodyMap.put("fail_url", "http://localhost:8000/order-service/order/pay/fail");

        String jsonRequestBody;
        // JSON 문자열로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonRequestBody = objectMapper.writeValueAsString(requestBodyMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // HttpEntity : HTTP 요청 또는 응답에 해당하는 Http Header와 Http Body를 포함하는 클래스
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, this.getHeaders());
        log.info("requestEntity의 내용 : {}", requestEntity);
        log.info("restTemplate 시작");

        RestTemplate template = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/ready";

        ResponseEntity<KakaoPayDTO> responseEntity = template.postForEntity(url, requestEntity, KakaoPayDTO.class);
        log.info("결제준비 응답객체: " + responseEntity.getBody());

        log.info("주문번호: " + partnerOrderId);
        SessionUtils.addAttribute(partnerOrderId, responseEntity.getBody().getTid());

        //Payment 엔티티에 tid와 orderId(Ordering 엔티티) 저장
        Payment newPayment = new Payment();
        newPayment.setOrdering(representativeOrder); // createOrder로 생성된 대표 주문 엔티티와 연결
        newPayment.setTid(responseEntity.getBody().getTid());
        newPayment.setSuccess(false); // 초기 상태는 false
        newPayment.setPaymentDate(LocalDateTime.now()); // 결제 준비 시점 시간

        newPayment.setPartnerOrderId(partnerOrderId); // Payment 엔티티에 partnerOrderId 필드가 있다면 추가

        paymentRepository.save(newPayment); // Payment 정보 저장


        return responseEntity.getBody();
    }

    // 카카오페이 결제 승인
    // 사용자가 결제 수단을 선택하고 비밀번호를 입력해 결제 인증을 완료한 뒤,
    // 최종적으로 결제 완료 처리를 하는 단계
    @Transactional
    public KakaoPayAproveResponse payApprove(String partnerOrderId, String pgToken) {
        log.info("결제 완료 처리 단계 시작");

        Payment foundPayment = paymentRepository.findByPartnerOrderId(partnerOrderId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다. partnerOrderId: " + partnerOrderId));

        String storedTid = foundPayment.getTid(); // Payment 엔티티에 저장된 tid 사용
        Ordering relatedOrder = foundPayment.getOrdering(); // Payment와 연결된 Ordering 엔티티

        if (relatedOrder == null) {
            throw new RuntimeException("결제와 연결된 주문 정보를 찾을 수 없습니다.");
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", "TC0ONETIME");              // 가맹점 코드(테스트용)
        parameters.put("tid", storedTid);                       // 결제 고유번호
        parameters.put("partner_order_id", partnerOrderId); // 주문번호
        parameters.put("partner_user_id", relatedOrder.getUserEmail());    // 회원 아이디
        parameters.put("pg_token", pgToken);              // 결제승인 요청을 인증하는 토큰

        String jsonRequestBody;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonRequestBody = objectMapper.writeValueAsString(parameters);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(parameters, this.getHeaders());

        RestTemplate template = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/approve";
//        ResponseEntity<KakaoPayAproveResponse> responseEntity = template.postForObject(url, requestEntity, KakaoPayAproveResponse.class);
//        KakaoPayAproveResponse approveResponse = responseEntity.getBody();
        KakaoPayAproveResponse approveResponse = template.postForObject(url, requestEntity, KakaoPayAproveResponse.class);
        log.info("결제승인 응답객체: " + approveResponse);

        // 결제 성공 후 Payment 엔티티 및 Ordering 엔티티 상태 업데이트
        foundPayment.setSuccess(true);
        foundPayment.setPaymentDate(LocalDateTime.now()); // 실제 결제 완료 시간으로 업데이트
        paymentRepository.save(foundPayment);

        // Ordering의 상태도 PAID로 업데이트
        relatedOrder.updateStatus(OrderStatus.ORDERED); // OrderStatus에 PAID 추가 필요 (또는 COMPLETED)
        orderingRepository.save(relatedOrder);

        return approveResponse;
    }

    // 카카오페이 측에 요청 시 헤더부에 필요한 값
    private HttpHeaders getHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // JSON으로 변경
        headers.set("Authorization", "SECRET_KEY " + secretKey);

        return headers;
    }


}