package com.lms.course.service;

import com.lms.course.dto.ModuleRequest;
import com.lms.course.dto.ModuleResponse;

import java.util.List;

public interface ModuleService {

    ModuleResponse addModule(ModuleRequest request);

    ModuleResponse getById(Long id);

    List<ModuleResponse> getByCourse(Long courseId);

    ModuleResponse update(Long id, ModuleRequest request);

    void delete(Long id);
}
