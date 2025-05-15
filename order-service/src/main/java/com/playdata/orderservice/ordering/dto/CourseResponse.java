package com.playdata.orderservice.ordering.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private Long productId;
    private String productName;
    private String description;
    private int price;
    private Long userId;
    private String category;
    private boolean active;
    private String filePath;
}