package com.sprintlog.sprintlogboot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weekly_goals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private int targetMinutes; // 이번 주 목표 학습 시간(분)

    public WeeklyGoal(int targetMinutes) {
        if (targetMinutes <= 0) {
            throw new IllegalArgumentException("주간 목표 시간은 1분 이상이어야 합니다.");
        }
        this.targetMinutes = targetMinutes;
    }

    public int achievementRate(int studiedMinutes) {
        if (studiedMinutes <= 0) return 0;
        int rate = (int) Math.round(studiedMinutes * 100.0 / targetMinutes);
        return Math.min(rate, 100);
    }

    public boolean isAchieved(int studiedMinutes) {
        return studiedMinutes >= targetMinutes;
    }

    public int remainingMinutes(int studiedMinutes) {
        return Math.max(0, this.targetMinutes - studiedMinutes);
    }
}












