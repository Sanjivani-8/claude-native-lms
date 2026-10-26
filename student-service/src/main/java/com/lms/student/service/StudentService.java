package com.lms.student.service;

import com.lms.student.dto.StudentProfileRequest;
import com.lms.student.dto.StudentProfileResponse;

import java.util.List;

public interface StudentService {

    StudentProfileResponse createProfile(StudentProfileRequest request);

    StudentProfileResponse getById(Long id);

    StudentProfileResponse getByUserId(Long userId);

    List<StudentProfileResponse> getAll();

    StudentProfileResponse updateProfile(Long id, StudentProfileRequest request);

    void deleteProfile(Long id);
}
