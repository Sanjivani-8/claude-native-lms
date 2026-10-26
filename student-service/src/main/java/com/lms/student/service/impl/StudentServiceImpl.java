package com.lms.student.service.impl;

import com.lms.student.dto.StudentProfileRequest;
import com.lms.student.dto.StudentProfileResponse;
import com.lms.student.entity.StudentProfile;
import com.lms.student.exception.DuplicateResourceException;
import com.lms.student.exception.ResourceNotFoundException;
import com.lms.student.repository.StudentProfileRepository;
import com.lms.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentProfileRepository studentProfileRepository;

    @Override
    public StudentProfileResponse createProfile(StudentProfileRequest request) {
        studentProfileRepository.findByUserId(request.getUserId()).ifPresent(existing -> {
            throw new DuplicateResourceException(
                    "A student profile already exists for userId " + request.getUserId());
        });

        StudentProfile profile = StudentProfile.builder()
                .userId(request.getUserId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .major(request.getMajor())
                .enrollmentYear(request.getEnrollmentYear())
                .build();

        StudentProfile saved = studentProfileRepository.save(profile);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getById(Long id) {
        return toResponse(findProfileOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getByUserId(Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student profile not found for userId " + userId));
        return toResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentProfileResponse> getAll() {
        return studentProfileRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StudentProfileResponse updateProfile(Long id, StudentProfileRequest request) {
        StudentProfile profile = findProfileOrThrow(id);

        studentProfileRepository.findByUserId(request.getUserId()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException(
                        "A student profile already exists for userId " + request.getUserId());
            }
        });

        profile.setUserId(request.getUserId());
        profile.setFullName(request.getFullName());
        profile.setEmail(request.getEmail());
        profile.setPhone(request.getPhone());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setMajor(request.getMajor());
        profile.setEnrollmentYear(request.getEnrollmentYear());

        StudentProfile saved = studentProfileRepository.save(profile);
        return toResponse(saved);
    }

    @Override
    public void deleteProfile(Long id) {
        StudentProfile profile = findProfileOrThrow(id);
        studentProfileRepository.delete(profile);
    }

    private StudentProfile findProfileOrThrow(Long id) {
        return studentProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for id " + id));
    }

    private StudentProfileResponse toResponse(StudentProfile profile) {
        return StudentProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .phone(profile.getPhone())
                .dateOfBirth(profile.getDateOfBirth())
                .major(profile.getMajor())
                .enrollmentYear(profile.getEnrollmentYear())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
