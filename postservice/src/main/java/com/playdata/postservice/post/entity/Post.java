package com.playdata.postservice.post.entity;


import com.playdata.postservice.post.dto.PostResDto;
import jakarta.persistence.*;
import lombok.*;
import com.playdata.postservice.common.entity.BaseTimeEntity;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Setter
@Table(name="tbl_post")
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn
    private Long userId;

    @JoinColumn
    private Long productId;

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private PostStatus status;


    public PostResDto fromEntity() {
        return PostResDto.builder()
                .id(id)
                .title(title)
                .content(content)
                .status(status)
                .userId(userId)
                .productId(productId)
                .build();
    }

}
