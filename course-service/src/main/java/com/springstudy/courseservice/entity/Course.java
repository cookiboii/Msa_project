package com.springstudy.courseservice.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;


    @Column(length = 2000)
    private String description;

    private int price;

    @Column(name = "user_id")
    private Long userId;  // 강사 ID (User Service의 유저 ID)

    private String category;  // 카테고리 필드 추가

    private boolean active; // 강의 활성화 여부

    private String filePath;      // 저장된 파일 경로 (또는 S3 URL)
}