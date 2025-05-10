package com.playdata.orderservice.ordering.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
//@Table(name = "order")
public class Ordering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 생성
    private Long id;

    @JoinColumn
    private Long userId;

    private String userEmail;

    private Long productId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.ORDERED;

    private LocalDate orderDate;



}
