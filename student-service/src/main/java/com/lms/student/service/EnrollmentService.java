package com.lms.student.service;

import com.lms.student.dto.EnrollmentRequest;
import com.lms.student.dto.EnrollmentResponse;
import com.lms.student.dto.ProgressUpdateRequest;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponse enroll(EnrollmentRequest request);

    EnrollmentResponse getById(Long id);

    List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId);

    List<EnrollmentResponse> getEnrollmentsByCourse(Long courseId);

    EnrollmentResponse updateProgress(Long enrollmentId, ProgressUpdateRequest request);

    EnrollmentResponse dropEnrollment(Long enrollmentId);

    void deleteEnrollment(Long enrollmentId);
}
