package com.springstudy.courseservice.service;

import com.springstudy.courseservice.dto.CourseRequest;
import com.springstudy.courseservice.dto.CourseResponse;
import com.springstudy.courseservice.entity.Course;
import com.springstudy.courseservice.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public CourseResponse createCourse(CourseRequest request, Long userId) {
        Course course = Course.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .price(request.getPrice())
                .userId(userId)
                .category(request.getCategory())
                .active(true)
                .filePath(request.getFilePath())
                .build();

        Course saved = courseRepository.save(course);
        return toResponse(saved);
    }

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        return toResponse(course);
    }

    // 페이징 조회
    public Page<CourseResponse> getCoursesByPage(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Course> coursePage = courseRepository.findAll(pageRequest);
        return coursePage.map(this::toResponse);
    }

    // 카테고리별 조회
    public List<CourseResponse> getCoursesByCategory(String category) {
        return courseRepository.findByCategory(category)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 제목 검색
    public List<CourseResponse> searchCourses(String keyword) {
        return courseRepository.findByProductNameContaining(keyword)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CourseResponse toResponse(Course course) {
        return CourseResponse.builder()
                .productId(course.getProductId())
                .productName(course.getProductName())
                .description(course.getDescription())
                .price(course.getPrice())
                .userId(course.getUserId())
                .category(course.getCategory())
                .active(course.isActive())
                .filePath(course.getFilePath())
                .build();
    }


    // 강의 수정
    public CourseResponse updateCourse(Long productId, CourseRequest request, Long userId) {
        Course course = courseRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("찾으시는 강의가 없습니다!"));

        if (!course.getUserId().equals(userId)) {
            throw new SecurityException("해당 강의의 수정 권한이 없습니다!");
        }

        course.setProductName(request.getProductName());
        course.setDescription(request.getDescription());
        course.setPrice(request.getPrice());
        course.setCategory(request.getCategory());
        course.setFilePath(request.getFilePath());

        Course updated = courseRepository.save(course);
        return toResponse(updated);
    }

    // 강의 삭제
    public void deleteCourse(Long productId, Long userId) {
        Course course = courseRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("찾으시는 강의가 없습니다!"));

        if (!course.getUserId().equals(userId)) {
            throw new SecurityException("해당 강의의 삭제 권한이 없습니다!");
        }

        courseRepository.delete(course);
    }

    public List<CourseResponse> getCourseById(List<Long> ids) {
        return ids.stream()
                .map(id -> courseRepository.findById(id)
                        .map(this::toResponse)
                        .orElseThrow(() -> new IllegalArgumentException("Course not found: " + id)))
                .collect(Collectors.toList());
    }


}
