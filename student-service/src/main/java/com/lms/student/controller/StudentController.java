package com.lms.student.controller;

import com.lms.student.dto.StudentProfileRequest;
import com.lms.student.dto.StudentProfileResponse;
import com.lms.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentProfileResponse> createProfile(@Valid @RequestBody StudentProfileRequest request) {
        StudentProfileResponse response = studentService.createProfile(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getById(id));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<StudentProfileResponse> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(studentService.getByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<List<StudentProfileResponse>> getAll() {
        return ResponseEntity.ok(studentService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentProfileResponse> updateProfile(@PathVariable Long id,
                                                                  @Valid @RequestBody StudentProfileRequest request) {
        return ResponseEntity.ok(studentService.updateProfile(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        studentService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}
