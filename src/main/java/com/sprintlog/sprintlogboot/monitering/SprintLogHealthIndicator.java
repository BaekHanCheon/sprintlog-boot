package com.sprintlog.sprintlogboot.monitering;

import com.sprintlog.sprintlogboot.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

//healthIndicator를 구현한 /actuator/health 항ㅇ 자동으로 합쳐진다.
@Component
@RequiredArgsConstructor
public class SprintLogHealthIndicator implements HealthIndicator {

  private final ActivityRepository activityRepository;

  @Override
  public Health health() {
    try {
      long count = activityRepository.count();
      return Health.up()
          .withDetail("count", count)
          .withDetail("message", "활동 데이터 정상 조회")
          .build();
    } catch (Exception e) {
      return Health.down(e).withDetail("message", "활동 데이터 조회 실패").build();
    }
  }
}
