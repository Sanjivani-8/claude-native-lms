package com.lms.assignment.service.impl;

import com.lms.assignment.dto.AssignmentRequest;
import com.lms.assignment.dto.AssignmentResponse;
import com.lms.assignment.entity.Assignment;
import com.lms.assignment.exception.ResourceNotFoundException;
import com.lms.assignment.repository.AssignmentRepository;
import com.lms.assignment.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;

    @Override
    public AssignmentResponse create(AssignmentRequest request) {
        Assignment assignment = Assignment.builder()
                .courseId(request.getCourseId())
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .maxScore(request.getMaxScore() != null ? request.getMaxScore() : 100)
                .createdBy(request.getCreatedBy())
                .build();
        Assignment saved = assignmentRepository.save(assignment);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getById(Long id) {
        return toResponse(findAssignmentOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getByCourse(Long courseId) {
        return assignmentRepository.findByCourseId(courseId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAll() {
        return assignmentRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AssignmentResponse update(Long id, AssignmentRequest request) {
        Assignment assignment = findAssignmentOrThrow(id);
        assignment.setCourseId(request.getCourseId());
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDueDate(request.getDueDate());
        if (request.getMaxScore() != null) {
            assignment.setMaxScore(request.getMaxScore());
        }
        if (request.getCreatedBy() != null) {
            assignment.setCreatedBy(request.getCreatedBy());
        }
        Assignment updated = assignmentRepository.save(assignment);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        Assignment assignment = findAssignmentOrThrow(id);
        assignmentRepository.delete(assignment);
    }

    private Assignment findAssignmentOrThrow(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
    }

    private AssignmentResponse toResponse(Assignment assignment) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .courseId(assignment.getCourseId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dueDate(assignment.getDueDate())
                .maxScore(assignment.getMaxScore())
                .createdBy(assignment.getCreatedBy())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}
