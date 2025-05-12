package com.playdata.orderservice.ordering.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ProdDetailResDto {
    private Long prodId;
    private String prodName;
    private String prodDesc;
    private String prodPrice;
    private String category;
    private String filePath;

}
