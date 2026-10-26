package com.lms.course.service.impl;

import com.lms.course.dto.ModuleRequest;
import com.lms.course.dto.ModuleResponse;
import com.lms.course.entity.Course;
import com.lms.course.entity.Module;
import com.lms.course.exception.ResourceNotFoundException;
import com.lms.course.repository.CourseRepository;
import com.lms.course.repository.ModuleRepository;
import com.lms.course.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

    @Override
    public ModuleResponse addModule(ModuleRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with id: " + request.getCourseId()));

        Module module = Module.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .orderIndex(request.getOrderIndex())
                .durationMinutes(request.getDurationMinutes())
                .build();

        module.setCourse(course);
        course.getModules().add(module);

        Module saved = moduleRepository.save(module);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleResponse getById(Long id) {
        return toResponse(findModuleOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleResponse> getByCourse(Long courseId) {
        return moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ModuleResponse update(Long id, ModuleRequest request) {
        Module module = findModuleOrThrow(id);

        if (!module.getCourse().getId().equals(request.getCourseId())) {
            Course newCourse = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Course not found with id: " + request.getCourseId()));
            module.setCourse(newCourse);
        }

        module.setTitle(request.getTitle());
        module.setContent(request.getContent());
        module.setOrderIndex(request.getOrderIndex());
        module.setDurationMinutes(request.getDurationMinutes());

        Module saved = moduleRepository.save(module);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Module module = findModuleOrThrow(id);
        moduleRepository.delete(module);
    }

    private Module findModuleOrThrow(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));
    }

    private ModuleResponse toResponse(Module module) {
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
