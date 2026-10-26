package com.lms.student.service.impl;

import com.lms.student.dto.EnrollmentRequest;
import com.lms.student.dto.EnrollmentResponse;
import com.lms.student.dto.ProgressUpdateRequest;
import com.lms.student.entity.Enrollment;
import com.lms.student.entity.EnrollmentStatus;
import com.lms.student.exception.DuplicateResourceException;
import com.lms.student.exception.ResourceNotFoundException;
import com.lms.student.repository.EnrollmentRepository;
import com.lms.student.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    @Override
    public EnrollmentResponse enroll(EnrollmentRequest request) {
        if (enrollmentRepository.existsByStudentIdAndCourseId(request.getStudentId(), request.getCourseId())) {
            throw new DuplicateResourceException(
                    "Student " + request.getStudentId() + " is already enrolled in course " + request.getCourseId());
        }

        Enrollment enrollment = Enrollment.builder()
                .studentId(request.getStudentId())
                .courseId(request.getCourseId())
                .status(EnrollmentStatus.ACTIVE)
                .progressPercent(0)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getById(Long id) {
        return toResponse(findEnrollmentOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EnrollmentResponse updateProgress(Long enrollmentId, ProgressUpdateRequest request) {
        Enrollment enrollment = findEnrollmentOrThrow(enrollmentId);

        enrollment.setProgressPercent(request.getProgressPercent());

        if (request.getProgressPercent() != null && request.getProgressPercent() >= 100) {
            enrollment.setProgressPercent(100);
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
        }

        Enrollment saved = enrollmentRepository.save(enrollment);
        return toResponse(saved);
    }

    @Override
    public EnrollmentResponse dropEnrollment(Long enrollmentId) {
        Enrollment enrollment = findEnrollmentOrThrow(enrollmentId);
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        Enrollment saved = enrollmentRepository.save(enrollment);
        return toResponse(saved);
    }

    @Override
    public void deleteEnrollment(Long enrollmentId) {
        Enrollment enrollment = findEnrollmentOrThrow(enrollmentId);
        enrollmentRepository.delete(enrollment);
    }

    private Enrollment findEnrollmentOrThrow(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for id " + id));
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudentId())
                .courseId(enrollment.getCourseId())
                .status(enrollment.getStatus())
                .progressPercent(enrollment.getProgressPercent())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .lastUpdated(enrollment.getLastUpdated())
                .build();
    }
}
