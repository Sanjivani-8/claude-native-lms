package com.lms.assignment.service;

import com.lms.assignment.dto.AssignmentRequest;
import com.lms.assignment.dto.AssignmentResponse;

import java.util.List;

public interface AssignmentService {

    AssignmentResponse create(AssignmentRequest request);

    AssignmentResponse getById(Long id);

    List<AssignmentResponse> getByCourse(Long courseId);

    List<AssignmentResponse> getAll();

    AssignmentResponse update(Long id, AssignmentRequest request);

    void delete(Long id);
}
