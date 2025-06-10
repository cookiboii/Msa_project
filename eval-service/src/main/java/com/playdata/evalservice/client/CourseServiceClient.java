package com.playdata.evalservice.client;

import com.playdata.evalservice.common.dto.CommonResDto;
import com.playdata.evalservice.eval.dto.CourseResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "course-service")
public interface CourseServiceClient {

    @GetMapping("/courses/find/userid")
    CommonResDto<CourseResDto> getIdByCourseId(@RequestParam Long courseId);

}
