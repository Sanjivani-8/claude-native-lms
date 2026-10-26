package com.lms.course.service.impl;

import com.lms.course.dto.CourseDetailResponse;
import com.lms.course.dto.CourseRequest;
import com.lms.course.dto.CourseResponse;
import com.lms.course.dto.ModuleResponse;
import com.lms.course.entity.Course;
import com.lms.course.entity.CourseStatus;
import com.lms.course.entity.Module;
import com.lms.course.exception.ResourceNotFoundException;
import com.lms.course.repository.CourseRepository;
import com.lms.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public CourseResponse create(CourseRequest request) {
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .instructorId(request.getInstructorId())
                .credits(request.getCredits())
                .category(request.getCategory())
                .status(request.getStatus() != null ? request.getStatus() : CourseStatus.DRAFT)
                .build();

        Course saved = courseRepository.save(course);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getById(Long id) {
        return toResponse(findCourseOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailResponse getDetailById(Long id) {
        return toDetailResponse(findCourseOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAll() {
        return courseRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getByStatus(CourseStatus status) {
        return courseRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getByCategory(String category) {
        return courseRepository.findByCategoryIgnoreCase(category).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = findCourseOrThrow(id);

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setInstructorId(request.getInstructorId());
        course.setCredits(request.getCredits());
        course.setCategory(request.getCategory());
        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }

        Course saved = courseRepository.save(course);
        return toResponse(saved);
    }

    @Override
    public CourseResponse publish(Long id) {
        Course course = findCourseOrThrow(id);
        course.setStatus(CourseStatus.PUBLISHED);
        return toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse archive(Long id) {
        Course course = findCourseOrThrow(id);
        course.setStatus(CourseStatus.ARCHIVED);
        return toResponse(courseRepository.save(course));
    }

    @Override
    public void delete(Long id) {
        Course course = findCourseOrThrow(id);
        courseRepository.delete(course);
    }

    private Course findCourseOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    private CourseResponse toResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .instructorId(course.getInstructorId())
                .credits(course.getCredits())
                .category(course.getCategory())
                .status(course.getStatus())
                .moduleCount(course.getModules() != null ? course.getModules().size() : 0)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private CourseDetailResponse toDetailResponse(Course course) {
        List<ModuleResponse> modules = course.getModules() == null ? List.of() :
                course.getModules().stream()
                        .sorted(Comparator.comparing(Module::getOrderIndex,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(this::toModuleResponse)
                        .collect(Collectors.toList());

        return CourseDetailResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .instructorId(course.getInstructorId())
                .credits(course.getCredits())
                .category(course.getCategory())
                .status(course.getStatus())
                .moduleCount(modules.size())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .modules(modules)
                .build();
    }

    private ModuleResponse toModuleResponse(Module module) {
        return ModuleResponse.builder()
                .id(module.getId())
                .courseId(module.getCourse() != null ? module.getCourse().getId() : null)
                .title(module.getTitle())
                .content(module.getContent())
                .orderIndex(module.getOrderIndex())
                .durationMinutes(module.getDurationMinutes())
                .createdAt(module.getCreatedAt())
                .build();
    }
}
