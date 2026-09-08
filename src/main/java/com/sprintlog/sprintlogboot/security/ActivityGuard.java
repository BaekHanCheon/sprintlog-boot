package com.sprintlog.sprintlogboot.security;

import com.sprintlog.sprintlogboot.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

//소유권 기반 인가 판단을 담당하는 빈
@Component("activityGuard")
@RequiredArgsConstructor
public class ActivityGuard {

  private final ActivityRepository repository;

  @Transactional(readOnly = true)
  public boolean isOwner (Long activityId, String email) {
    return repository.findById(activityId)
        .map(activity -> activity.getOwner() != null && email.equals(activity.getOwner().getEmail()))
        .orElse(false);
  }

}
