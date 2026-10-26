package com.lms.course.service;

import com.lms.course.dto.CourseRequest;
import com.lms.course.dto.CourseResponse;
import com.lms.course.entity.Course;
import com.lms.course.entity.CourseStatus;
import com.lms.course.exception.ResourceNotFoundException;
import com.lms.course.repository.CourseRepository;
import com.lms.course.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course course;

    @BeforeEach
    void setUp() {
        course = Course.builder()
                .id(1L)
                .title("Intro to Spring Boot")
                .description("Learn the basics of Spring Boot")
                .instructorId(100L)
                .credits(3)
                .category("Backend")
                .status(CourseStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void create_shouldPersistAndReturnCourseResponse() {
        CourseRequest request = CourseRequest.builder()
                .title("Intro to Spring Boot")
                .description("Learn the basics of Spring Boot")
                .instructorId(100L)
                .credits(3)
                .category("Backend")
                .build();

        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Intro to Spring Boot");
        assertThat(response.getStatus()).isEqualTo(CourseStatus.DRAFT);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void publish_shouldSetStatusToPublished() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponse response = courseService.publish(1L);

        assertThat(response.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        verify(courseRepository).save(course);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenCourseMissing() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
