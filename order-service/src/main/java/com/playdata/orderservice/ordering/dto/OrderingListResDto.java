package com.playdata.orderservice.ordering.dto;

import com.playdata.orderservice.ordering.entity.OrderStatus;
import com.playdata.orderservice.ordering.entity.Ordering;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderingListResDto {

    // 하나의 주문에 대한 내용
    private Long id;
    private String userEmail;
    private OrderStatus orderStatus;
    private Long productId;
    private String productName;
    private LocalDate orderDate;
    private String category;
    private String filePath;

    // Entity → DTO 변환
    public static OrderingListResDto fromEntity(Ordering ordering) {
//        Product product = ordering.getProduct();

        return OrderingListResDto.builder()
            .id(ordering.getId())
            .userEmail("mockuser@example.com") // 실제 유저 정보 없으므로 임시
            .productId(ordering.getProductId())
            .productName("강의이름")
            .orderStatus(ordering.getOrderStatus())
            .orderDate(ordering.getOrderDate()) // LocalDateTime → LocalDate
            .build();
    }

}
