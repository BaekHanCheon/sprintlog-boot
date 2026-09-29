package com.sprintlog.sprintlogboot.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sprintlog.sprintlogboot.domain.*;

import java.time.LocalDate;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL) // 값이 비어있는 필드는 JSON에서 아예 빼버려라.
public record ActivityResponse(
        long id,
        ActivityCategory category,   // 활동 종류(LECTURE/PRACTICE/READING)
        String title,
        int minutes,
        Visibility visibility,
        Set<String> tags,

        // 하위 타입별 상세 — 해당 타입일 때만 채워지고, 나머지는 null 이라 JSON 에서 생략된다.
        String instructorName,       // LECTURE 전용
        Integer completionRate,      // PRACTICE 전용
        String bookTitle,             // READING 전용

        // 연관관계 세팅 후 활동 객체 조회 시 활동을 추가한 user의 정보도 함께 응답
        Long ownerId,
        String onwerNickname,

        LocalDate studiedOn

) {

    /**
     * 도메인 엔티티 → 응답 DTO 로 변환하는 정적 팩토리.
     * 상속 구조를 없앴기 때문에 단순히 getter로 읽으면 되고, @JsonInclude를 선언해 놓았기 때문에
     * null값이라면 알아서 JSON에서 생략된다.
     */
    public static ActivityResponse from(LearningActivity activity) {
        User owner = activity.getOwner();
        Long ownerId = (owner != null) ? owner.getId() : null;
        String ownerNickname = (owner != null) ? owner.getNickname() : null;

        return new ActivityResponse(
                activity.getId(),
                activity.getCategory(),
                activity.getTitle(),
                activity.getMinutes(),
                activity.getVisibility(),
                activity.getTags(),
                activity.getInstructorName(),
                activity.getCompletionRate(),
                activity.getBookTitle(),
                ownerId,
                ownerNickname,
                activity.getStudiedOn());
    }
}