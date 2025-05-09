package com.playdata.postservice.client;

import com.playdata.postservice.common.dto.CommonResDto;
import com.playdata.postservice.post.dto.CourseResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "product-service")
public interface CourseServiceClient {

    @GetMapping("/course/user-id")
    CommonResDto<CourseResDto> getIdByCourseId(Long id);

}
