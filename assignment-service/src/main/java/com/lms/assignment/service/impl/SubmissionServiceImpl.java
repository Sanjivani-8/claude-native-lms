package com.lms.assignment.service.impl;

import com.lms.assignment.dto.GradeRequest;
import com.lms.assignment.dto.SubmissionRequest;
import com.lms.assignment.dto.SubmissionResponse;
import com.lms.assignment.entity.Assignment;
import com.lms.assignment.entity.Submission;
import com.lms.assignment.exception.DuplicateResourceException;
import com.lms.assignment.exception.ResourceNotFoundException;
import com.lms.assignment.repository.AssignmentRepository;
import com.lms.assignment.repository.SubmissionRepository;
import com.lms.assignment.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;

    @Override
    public SubmissionResponse submit(SubmissionRequest request) {
        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assignment not found with id: " + request.getAssignmentId()));

        submissionRepository.findByAssignmentIdAndStudentId(request.getAssignmentId(), request.getStudentId())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Student " + request.getStudentId() + " has already submitted assignment "
                                    + assignment.getId());
                });

        Submission submission = Submission.builder()
                .assignmentId(request.getAssignmentId())
                .studentId(request.getStudentId())
                .content(request.getContent())
                .graded(false)
                .build();
        Submission saved = submissionRepository.save(submission);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getById(Long id) {
        return toResponse(findSubmissionOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SubmissionResponse grade(Long submissionId, GradeRequest request) {
        Submission submission = findSubmissionOrThrow(submissionId);
        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assignment not found with id: " + submission.getAssignmentId()));

        if (assignment.getMaxScore() != null && request.getScore() > assignment.getMaxScore()) {
            throw new IllegalArgumentException(
                    "Score " + request.getScore() + " exceeds assignment max score of " + assignment.getMaxScore());
        }

        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setGradedBy(request.getGradedBy());
        submission.setGraded(true);
        submission.setGradedAt(LocalDateTime.now());

        Submission graded = submissionRepository.save(submission);
        return toResponse(graded);
    }

    private Submission findSubmissionOrThrow(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));
    }

    private SubmissionResponse toResponse(Submission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignmentId())
                .studentId(submission.getStudentId())
                .content(submission.getContent())
                .submittedAt(submission.getSubmittedAt())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .graded(submission.isGraded())
                .gradedAt(submission.getGradedAt())
                .gradedBy(submission.getGradedBy())
                .build();
    }
}
