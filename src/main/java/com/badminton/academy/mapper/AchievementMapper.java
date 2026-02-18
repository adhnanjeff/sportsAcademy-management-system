package com.badminton.academy.mapper;

import com.badminton.academy.dto.request.CreateAchievementRequest;
import com.badminton.academy.dto.response.AchievementResponse;
import com.badminton.academy.model.Achievement;
import org.springframework.stereotype.Component;

@Component
public class AchievementMapper {

    public AchievementResponse toResponse(Achievement achievement) {
        if (achievement == null) return null;

        AchievementResponse response = new AchievementResponse();
        response.setId(achievement.getId());
        response.setTitle(achievement.getTitle());
        response.setDescription(achievement.getDescription());
        response.setType(achievement.getType());
        response.setEventName(achievement.getEventName());
        response.setPosition(achievement.getPosition());
        response.setAchievedDate(achievement.getAchievedDate());
        response.setCertificateUrl(achievement.getCertificateUrl());
        response.setAwardedBy(achievement.getAwardedBy());
        response.setIsVerified(achievement.getIsVerified());

        if (achievement.getStudent() != null) {
            response.setStudentId(achievement.getStudent().getId());
            response.setStudentName(achievement.getStudent().getFullName());
        }

        if (achievement.getVerifiedBy() != null) {
            response.setVerifiedById(achievement.getVerifiedBy().getId());
            response.setVerifiedByName(achievement.getVerifiedBy().getFullName());
        }

        return response;
    }

    public Achievement toEntity(CreateAchievementRequest request) {
        if (request == null) return null;

        Achievement achievement = new Achievement();
        achievement.setTitle(request.getTitle());
        achievement.setDescription(request.getDescription());
        achievement.setType(request.getType());
        achievement.setEventName(request.getEventName());
        achievement.setPosition(request.getPosition());
        achievement.setAchievedDate(request.getAchievedDate());
        achievement.setCertificateUrl(request.getCertificateUrl());
        achievement.setAwardedBy(request.getAwardedBy());
        achievement.setIsVerified(false);
        return achievement;
    }

    public void updateEntityFromRequest(CreateAchievementRequest request, Achievement achievement) {
        if (request == null || achievement == null) return;

        if (request.getTitle() != null) achievement.setTitle(request.getTitle());
        if (request.getDescription() != null) achievement.setDescription(request.getDescription());
        if (request.getType() != null) achievement.setType(request.getType());
        if (request.getEventName() != null) achievement.setEventName(request.getEventName());
        if (request.getPosition() != null) achievement.setPosition(request.getPosition());
        if (request.getAchievedDate() != null) achievement.setAchievedDate(request.getAchievedDate());
        if (request.getCertificateUrl() != null) achievement.setCertificateUrl(request.getCertificateUrl());
        if (request.getAwardedBy() != null) achievement.setAwardedBy(request.getAwardedBy());
    }
}
