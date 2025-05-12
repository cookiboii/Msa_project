package com.springstudy.courseservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CourseResDto {

    private Long productId;
    private String productName;
    private String description;
    private int price;
    private Long userId;
    private String category;
    private boolean active;

}
