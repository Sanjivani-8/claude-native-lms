package com.lms.assignment.service;

import com.lms.assignment.dto.GradeRequest;
import com.lms.assignment.dto.SubmissionRequest;
import com.lms.assignment.dto.SubmissionResponse;
import com.lms.assignment.entity.Assignment;
import com.lms.assignment.entity.Submission;
import com.lms.assignment.repository.AssignmentRepository;
import com.lms.assignment.repository.SubmissionRepository;
import com.lms.assignment.service.impl.SubmissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceImplTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private Assignment assignment;

    @BeforeEach
    void setUp() {
        assignment = Assignment.builder()
                .id(1L)
                .courseId(10L)
                .title("Homework 1")
                .maxScore(100)
                .build();
    }

    @Test
    void submit_happyPath_savesSubmission() {
        SubmissionRequest request = SubmissionRequest.builder()
                .assignmentId(1L)
                .studentId(20L)
                .content("my submission content")
                .build();

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(submissionRepository.findByAssignmentIdAndStudentId(1L, 20L)).thenReturn(Optional.empty());

        Submission savedSubmission = Submission.builder()
                .id(100L)
                .assignmentId(1L)
                .studentId(20L)
                .content("my submission content")
                .graded(false)
                .build();
        when(submissionRepository.save(any(Submission.class))).thenReturn(savedSubmission);

        SubmissionResponse response = submissionService.submit(request);

        assertEquals(100L, response.getId());
        assertEquals(1L, response.getAssignmentId());
        assertEquals(20L, response.getStudentId());
        assertTrue(!response.isGraded());

        ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository).save(captor.capture());
        assertEquals("my submission content", captor.getValue().getContent());
    }

    @Test
    void grade_setsGradedTrue() {
        Submission existing = Submission.builder()
                .id(100L)
                .assignmentId(1L)
                .studentId(20L)
                .content("my submission content")
                .graded(false)
                .build();

        GradeRequest gradeRequest = GradeRequest.builder()
                .score(90)
                .feedback("Great work")
                .gradedBy(5L)
                .build();

        when(submissionRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubmissionResponse response = submissionService.grade(100L, gradeRequest);

        assertTrue(response.isGraded());
        assertEquals(90, response.getScore());
        assertEquals("Great work", response.getFeedback());
        assertEquals(5L, response.getGradedBy());
        assertEquals(true, response.isGraded());
    }

    @Test
    void grade_scoreExceedsMax_throwsIllegalArgumentException() {
        Submission existing = Submission.builder()
                .id(100L)
                .assignmentId(1L)
                .studentId(20L)
                .content("my submission content")
                .graded(false)
                .build();

        GradeRequest gradeRequest = GradeRequest.builder()
                .score(150)
                .feedback("Too high")
                .gradedBy(5L)
                .build();

        when(submissionRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        assertThrows(IllegalArgumentException.class, () -> submissionService.grade(100L, gradeRequest));

        verify(submissionRepository, never()).save(any(Submission.class));
    }
}
