package com.lms.assignment.service;

import com.lms.assignment.dto.GradeRequest;
import com.lms.assignment.dto.SubmissionRequest;
import com.lms.assignment.dto.SubmissionResponse;

import java.util.List;

public interface SubmissionService {

    SubmissionResponse submit(SubmissionRequest request);

    SubmissionResponse getById(Long id);

    List<SubmissionResponse> getByAssignment(Long assignmentId);

    List<SubmissionResponse> getByStudent(Long studentId);

    SubmissionResponse grade(Long submissionId, GradeRequest request);
}
