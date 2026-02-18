package com.badminton.academy.service;

import com.badminton.academy.dto.request.CreateBatchRequest;
import com.badminton.academy.dto.request.UpdateBatchRequest;
import com.badminton.academy.dto.response.BatchResponse;
import java.util.List;

public interface IBatchService {
    BatchResponse createBatch(CreateBatchRequest request);
    BatchResponse getBatchById(Long id);
    List<BatchResponse> getAllBatches();
    List<BatchResponse> getBatchesByCoachId(Long coachId);
    List<BatchResponse> getActiveBatches();
    BatchResponse updateBatch(Long id, UpdateBatchRequest request);
    void deleteBatch(Long id);
    void addStudentToBatch(Long batchId, Long studentId);
    void removeStudentFromBatch(Long batchId, Long studentId);
}
