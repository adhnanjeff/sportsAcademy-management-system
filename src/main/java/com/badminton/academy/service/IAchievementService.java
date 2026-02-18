package com.badminton.academy.service;

import com.badminton.academy.dto.request.CreateAchievementRequest;
import com.badminton.academy.dto.response.AchievementResponse;
import java.util.List;

public interface IAchievementService {
    AchievementResponse createAchievement(CreateAchievementRequest request);
    AchievementResponse getAchievementById(Long id);
    List<AchievementResponse> getAchievementsByStudentId(Long studentId);
    List<AchievementResponse> getAllAchievements();
    AchievementResponse updateAchievement(Long id, CreateAchievementRequest request);
    void deleteAchievement(Long id);
    void verifyAchievement(Long id, Long coachId);
}
