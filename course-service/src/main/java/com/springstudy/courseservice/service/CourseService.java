package com.springstudy.courseservice.service;

import com.springstudy.courseservice.client.UserServiceClient;
import com.springstudy.courseservice.common.auth.TokenUserInfo;
import com.springstudy.courseservice.dto.CourseRequestDto;
import com.springstudy.courseservice.dto.CourseResponseDto;
import com.springstudy.courseservice.dto.UserResDto;
import com.springstudy.courseservice.entity.Course;
import com.springstudy.courseservice.common.exception.CourseNotFoundException;
import com.springstudy.courseservice.common.exception.UnauthorizedCourseAccessException;
import com.springstudy.courseservice.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.springstudy.courseservice.user.entity.User;
import com.springstudy.courseservice.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j

public class CourseService {

    private final CourseRepository courseRepository;
    private final UserServiceClient userServiceClient;
    private final UserRepository userRepository;

    public List<Course> createCourse(TokenUserInfo userInfo, List<CourseRequestDto> dtoList) {
        UserResDto userResDto = userServiceClient.findByEmail(userInfo.getEmail()).getResult();

        List<Course> courses = new ArrayList<>();
        for (CourseRequestDto courseRequest : dtoList) {
            Course course = Course.builder()
                    .productName(courseRequest.getProductName())
                    .description(courseRequest.getDescription())
                    .price(courseRequest.getPrice())
                    .userId(userResDto.getId())
                    .category(courseRequest.getCategory())
                    .active(true)
                    .filePath(courseRequest.getFilePath())
                    .build();

            courses.add(course);
            courseRepository.save(course);
            log.info("Course created: {}", course.getProductName());
        }

        return courses;
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable)
                .stream()
                .filter(Course::isActive)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseResponseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
        if(!course.isActive()) {
            throw new CourseNotFoundException(id);
        }
        return toResponse(course);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDto> getCoursesByPage(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Course> coursePage = courseRepository.findAll(pageRequest);

        List<CourseResponseDto> filtered = coursePage.stream()
                .filter(Course::isActive) // active == true인 것만
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(filtered, pageRequest, filtered.size());
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDto> getCoursesByCategory(String category) {
        int page = 0;
//        page = page - 1;
        int size = 12;
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Course> byCategory = courseRepository.findByCategory(category, pageRequest);

        List<CourseResponseDto> filtered = byCategory.stream()
                .filter(Course::isActive)
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(filtered, pageRequest, filtered.size());
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> searchCourses(String keyword) {
        return courseRepository.findByProductNameContaining(keyword)
                .stream()
                .filter(Course::isActive)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CourseResponseDto updateCourse(Long productId, CourseRequestDto courseRequestDto, TokenUserInfo userInfo) {
        UserResDto userResDto = userServiceClient.findByEmail(userInfo.getEmail()).getResult();

        Course course = courseRepository.findById(productId)
                .orElseThrow(() -> new CourseNotFoundException(productId));

        if (!course.getUserId().equals(userResDto.getId())) {
            throw new UnauthorizedCourseAccessException("해당 강의의 수정 권한이 없습니다!");
        }

       course.updateCourseInfo(courseRequestDto);


        return toResponse(course);
    }

    public void deleteCourse(Long productId, TokenUserInfo userInfo) {
        UserResDto userResDto = userServiceClient.findByEmail(userInfo.getEmail()).getResult();

        Course course = courseRepository.findById(productId)
                .orElseThrow(() -> new CourseNotFoundException(productId));

        if (!course.getUserId().equals(userResDto.getId())) {
            throw new UnauthorizedCourseAccessException("해당 강의의 삭제 권한이 없습니다!");
        }

       //        courseRepository.delete(course);

        course.deactivate();
        courseRepository.save(course);
        log.info("Course deleted: {}", productId);
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDto> getCourseById(List<Long> ids) {
        return ids.stream()
                .map(id -> courseRepository.findById(id)
                        .filter(Course::isActive) // active == true인 경우만 통과
                        .map(this::toResponse)
                        .orElseThrow(() -> new CourseNotFoundException(id)))
                .collect(Collectors.toList());
    }

    private CourseResponseDto toResponse(Course course) {
        // userId로 유저 이름 조회
        String username = userRepository.findById(course.getUserId())
                .map(User::getUsername)
                .orElse("Unknown");

        return CourseResponseDto.builder()
                .productId(course.getProductId())
                .productName(course.getProductName())
                .description(course.getDescription())
                .price(course.getPrice())
                .userId(course.getUserId())
                .category(course.getCategory())
                .active(course.isActive())
                .filePath(course.getFilePath())
                .username(username)
                .build();
    }

    public List<Course> findByUserId(Long userId) {

        List<Course> byUserId = courseRepository.findByUserId(userId);
        return byUserId.stream()
                .filter(Course::isActive) // 또는 course -> course.getActive()
                .collect(Collectors.toList());
    }
}
