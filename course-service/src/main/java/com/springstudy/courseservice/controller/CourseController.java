package com.springstudy.courseservice.controller;

import com.springstudy.courseservice.common.dto.CommonResDto;
import com.springstudy.courseservice.dto.CourseRequest;
import com.springstudy.courseservice.dto.CourseResDto;
import com.springstudy.courseservice.dto.CourseResponse;
import com.springstudy.courseservice.entity.Course;
import com.springstudy.courseservice.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.juli.logging.Log;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // 강의 등록 (강사용)
    @PostMapping("/create")
    public ResponseEntity<CourseResponse> createCourse(@RequestBody CourseRequest request,
                                                       @RequestHeader("userId") Long userId) {
        return ResponseEntity.ok(courseService.createCourse(request, userId));
    }

    // 강의 목록
    @GetMapping("/list")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // 페이징 조회
    @GetMapping("/list")
    public ResponseEntity<Page<CourseResponse>> getCoursesByPage(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(courseService.getCoursesByPage(page, size));
    }

    // 카테고리별 조회
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<CourseResponse>> getCoursesByCategory(@PathVariable String category,
    @RequestParam int page) {
        return ResponseEntity.ok(courseService.getCoursesByCategory(category, page));
    }

    // 검색
    @GetMapping("/search")
    public ResponseEntity<List<CourseResponse>> searchCourses(@RequestParam String keyword) {
        return ResponseEntity.ok(courseService.searchCourses(keyword));
    }


    // 강의 상세
    @GetMapping("/info/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }


    // 강의 수정
    @PutMapping("/edit/{id}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long id,
                                                       @RequestBody CourseRequest request,
                                                       @RequestHeader("userId") Long userId) {
        return ResponseEntity.ok(courseService.updateCourse(id, request, userId));
    }

    // 강의 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id,
                                             @RequestHeader("userId") Long userId) {
        courseService.deleteCourse(id, userId);
        return ResponseEntity.noContent().build();
    }

    // 한 사용자의 모든 주문 내역 안에 있는 상품 정보를 리턴하는 메서드
    @PostMapping("/products")
    public ResponseEntity<?> getProducts(@RequestBody List<Long> productIds) {
        List<CourseResponse> productDtos = courseService.getCourseById(productIds);

        log.info(productDtos.toString());

        // CommonResDto로 감싸서 반환
        CommonResDto<List<CourseResponse>> resDto = new CommonResDto<>(HttpStatus.OK, "조회 완료", productDtos);

        log.info(resDto.toString());

        return ResponseEntity.ok(resDto);
    }


    // 댓글 작성 시 강사 userId 조회를 위한 메소드입니다.
    @GetMapping("/find/userid")
    public CommonResDto<?> getuserIdByCourseId(@RequestParam Long courseId) {
        CourseResponse foundCourse = courseService.getCourseById(courseId);

        CourseResDto build = CourseResDto.builder()
                .productId(foundCourse.getProductId())
                .productName(foundCourse.getProductName())
                .price(foundCourse.getPrice())
                .description(foundCourse.getDescription())
                .userId(foundCourse.getUserId())
                .category(foundCourse.getCategory())
                .active(foundCourse.isActive())
                .build();

        return new CommonResDto(HttpStatus.OK,"해당 강의 찾음" ,build);

    }
}