package com.lms.student.service;

import com.lms.student.dto.EnrollmentRequest;
import com.lms.student.dto.EnrollmentResponse;
import com.lms.student.dto.ProgressUpdateRequest;
import com.lms.student.entity.Enrollment;
import com.lms.student.entity.EnrollmentStatus;
import com.lms.student.exception.DuplicateResourceException;
import com.lms.student.exception.ResourceNotFoundException;
import com.lms.student.repository.EnrollmentRepository;
import com.lms.student.service.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private EnrollmentRequest enrollmentRequest;

    @BeforeEach
    void setUp() {
        enrollmentRequest = EnrollmentRequest.builder()
                .studentId(1L)
                .courseId(10L)
                .build();
    }

    @Test
    void enroll_shouldCreateEnrollment_whenNotAlreadyEnrolled() {
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(false);

        Enrollment saved = Enrollment.builder()
                .id(100L)
                .studentId(1L)
                .courseId(10L)
                .status(EnrollmentStatus.ACTIVE)
                .progressPercent(0)
                .enrolledAt(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(saved);

        EnrollmentResponse response = enrollmentService.enroll(enrollmentRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStudentId()).isEqualTo(1L);
        assertThat(response.getCourseId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(response.getProgressPercent()).isEqualTo(0);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getStudentId()).isEqualTo(1L);
        assertThat(captor.getValue().getCourseId()).isEqualTo(10L);
    }

    @Test
    void enroll_shouldThrowDuplicateResourceException_whenAlreadyEnrolled() {
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> enrollmentService.enroll(enrollmentRequest));

        verify(enrollmentRepository, times(0)).save(any(Enrollment.class));
    }

    @Test
    void updateProgress_shouldMarkCompleted_whenProgressReaches100() {
        Enrollment existing = Enrollment.builder()
                .id(5L)
                .studentId(1L)
                .courseId(10L)
                .status(EnrollmentStatus.ACTIVE)
                .progressPercent(60)
                .enrolledAt(LocalDateTime.now().minusDays(2))
                .lastUpdated(LocalDateTime.now().minusDays(1))
                .build();

        when(enrollmentRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProgressUpdateRequest request = ProgressUpdateRequest.builder()
                .progressPercent(100)
                .build();

        EnrollmentResponse response = enrollmentService.updateProgress(5L, request);

        assertThat(response.getProgressPercent()).isEqualTo(100);
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(response.getCompletedAt()).isNotNull();
    }

    @Test
    void updateProgress_shouldThrowResourceNotFoundException_whenEnrollmentMissing() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        ProgressUpdateRequest request = ProgressUpdateRequest.builder()
                .progressPercent(50)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.updateProgress(99L, request));
    }
}
