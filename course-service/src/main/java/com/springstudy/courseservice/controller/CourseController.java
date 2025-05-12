package com.springstudy.courseservice.controller;

import com.springstudy.courseservice.dto.CourseRequest;
import com.springstudy.courseservice.dto.CourseResponse;
import com.springstudy.courseservice.service.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping("/page/{page}")
    public ResponseEntity<Page<CourseResponse>> getCoursesByPage(@PathVariable int page, @RequestParam int size) {
        return ResponseEntity.ok(courseService.getCoursesByPage(page, size));
    }

    // 카테고리별 조회
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseResponse>> getCoursesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(courseService.getCoursesByCategory(category));
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

}