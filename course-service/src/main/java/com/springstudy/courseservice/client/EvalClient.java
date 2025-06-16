package com.springstudy.courseservice.client;

import com.springstudy.courseservice.common.dto.CommonResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "eval-service")
public interface EvalClient {

    @PostMapping("/eval/course-eval-rating-feign")
    CommonResDto<Map<Long, Double>> findCoursesRatingFeign(@RequestBody List<Long> prodIdList);
}
