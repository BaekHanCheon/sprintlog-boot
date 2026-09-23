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

  // JWT의 uid로 소유권을 검사한다. uid가 없는 구 토큰에는 소유권을 부여하지 않는다.
  @Transactional(readOnly = true)
  public boolean isOwner(Long activityId, Long userId) {
    if (userId == null) {
      return false;
    }
    return repository.findById(activityId)
        .map(activity -> activity.getOwner() != null
            && userId.equals(activity.getOwner().getId()))
        .orElse(false);
  }

  @Transactional(readOnly = true)
  public boolean isOwner (Long activityId, String email) {
    return repository.findById(activityId)
        .map(activity -> activity.getOwner() != null && email.equals(activity.getOwner().getEmail()))
        .orElse(false);
  }

}
