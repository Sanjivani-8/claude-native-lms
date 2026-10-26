package com.lms.course.service;

import com.lms.course.dto.CourseDetailResponse;
import com.lms.course.dto.CourseRequest;
import com.lms.course.dto.CourseResponse;
import com.lms.course.entity.CourseStatus;

import java.util.List;

public interface CourseService {

    CourseResponse create(CourseRequest request);

    CourseResponse getById(Long id);

    CourseDetailResponse getDetailById(Long id);

    List<CourseResponse> getAll();

    List<CourseResponse> getByInstructor(Long instructorId);

    List<CourseResponse> getByStatus(CourseStatus status);

    List<CourseResponse> getByCategory(String category);

    CourseResponse update(Long id, CourseRequest request);

    CourseResponse publish(Long id);

    CourseResponse archive(Long id);

    void delete(Long id);
}
