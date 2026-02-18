package com.badminton.academy.service;

import com.badminton.academy.dto.request.UpdateStudentRequest;
import com.badminton.academy.dto.response.StudentResponse;
import com.badminton.academy.model.enums.SkillLevel;
import java.util.List;

public interface IStudentService {
    StudentResponse getStudentById(Long id);
    List<StudentResponse> getAllStudents();
    List<StudentResponse> getStudentsBySkillLevel(SkillLevel skillLevel);
    List<StudentResponse> getStudentsByParentId(Long parentId);
    List<StudentResponse> getStudentsByBatchId(Long batchId);
    StudentResponse updateStudent(Long id, UpdateStudentRequest request);
    void deleteStudent(Long id);
}
