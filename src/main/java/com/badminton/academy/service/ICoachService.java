package com.badminton.academy.service;

import com.badminton.academy.dto.request.UpdateCoachRequest;
import com.badminton.academy.dto.response.CoachResponse;
import java.util.List;

public interface ICoachService {
    CoachResponse getCoachById(Long id);
    List<CoachResponse> getAllCoaches();
    List<CoachResponse> getActiveCoaches();
    CoachResponse updateCoach(Long id, UpdateCoachRequest request);
    void deleteCoach(Long id);
}
