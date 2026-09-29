package com.sprintlog.sprintlogboot.repository;

import com.sprintlog.sprintlogboot.domain.WeeklyGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeeklyGoalRepository extends JpaRepository<WeeklyGoal, Long> {

    Optional<WeeklyGoal> findByUserId(Long userId);

}
