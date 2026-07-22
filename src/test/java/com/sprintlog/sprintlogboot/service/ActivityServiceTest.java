package com.sprintlog.sprintlogboot.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import com.sprintlog.sprintlogboot.domain.ActivityCategory;
import com.sprintlog.sprintlogboot.domain.LearningActivity;
import com.sprintlog.sprintlogboot.domain.Visibility;
import com.sprintlog.sprintlogboot.dto.request.CreateActivityRequest;
import com.sprintlog.sprintlogboot.dto.request.UpdateActivityRequest;
import com.sprintlog.sprintlogboot.exception.ActivityNotFoundException;
import com.sprintlog.sprintlogboot.repository.ActivityRepository;
import com.sprintlog.sprintlogboot.repository.AuditLogRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityService 슬라이스 테스트 (Mokito)")
class ActivityServiceTest {

  @Mock
  ActivityRepository repository;
  @Mock
  AuditService auditService;
  @Mock
  AuditLogRepository auditLogRepository;
  
  //service의 create는 timer가 걸려있음 -> meterRegistry의 timer는 실제로 동작해야 함 (Mock안됨)
  @Spy //실제 기능이 동작할 수 있는 객체로 둠
  MeterRegistry meterRegistry = new SimpleMeterRegistry(); //meterregistry를 상속받는 구현체중 간단하게 사용가능한 simplemeterregistry 사용

  @InjectMocks
  ActivityService service; // 가짜 객체들을 주입받은 Service 객체를 사용

  private LearningActivity sample() {
    return new LearningActivity(ActivityCategory.LECTURE, "옛 제목", 30, Visibility.PRIVATE, "이강사", null, null);
  }

  @Nested
  @DisplayName("조회(get)")
  class Get {

    @Test
    @DisplayName("있으면 그 활동을 돌려준다.")
    void 있으면_반환() {
        // given
        LearningActivity activity = sample();
        when(repository.findById(activity.getId())).thenReturn(java.util.Optional.of(activity));


        // when then
        assertThat(service.get(1L)).isSameAs(activity);

    }

    @Test
    @DisplayName("없으면 ActivityNotFoundException 발생")
    void 없으면_예외() {
        // given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // when then
        assertThatThrownBy(() -> service.get(999L)).isInstanceOf(ActivityNotFoundException.class);

    }
  }
  /// --------------------------------------------------------------------------------
  @Nested
  @DisplayName("수정update")
  class Update {
    
    @Test
    @DisplayName("제목, 공개 여부를 바꾸고 저장한다.")
    void 정상_수정 () {
        // given
        LearningActivity activity = sample();
        given(repository.findById(1L)).willReturn(Optional.of(activity)); //BDD방식의 Mockito 방식 최근 선호
        given(repository.save(any())).willReturn(activity);
    
        // when
        service.update(1L, new UpdateActivityRequest("새 제목", Visibility.PUBLIC));
    
        // then
        assertThat(activity.getTitle()).isEqualTo("새 제목"); //상태 검증
        assertThat(activity.getVisibility()).isEqualTo(Visibility.PUBLIC); //상태 검증
        verify(repository).save(activity); //행위 검증(서비스가 협력자를 제대로 불렀나)
    }

    @Test
    @DisplayName("대상이 없다면 예외 - 저장 일어나지 않음")
    void 없으면_변경없음() {
        // given
        given(repository.findById(999L)).willReturn(Optional.empty());

        // when then
        assertThatThrownBy(()->service.update(999L, new UpdateActivityRequest("x", Visibility.PUBLIC)))
          .isInstanceOf(ActivityNotFoundException.class);
        verify(repository, never()).save(any());
    }
  }
  /// --------------------------------------------------------------------------------

  @Nested
  @DisplayName("삭제(Delete)")
  class Delete{
    @Test
    @DisplayName("존재하면 그 id로 삭제한다. (existsById 확인)")
    void 정상_삭제() {
        // given
      given(repository.existsById(1L)).willReturn(true);

        // when
      service.delete(1L);

      // then

      verify(repository).deleteById(1L); //행위 검증(서비스가 협력자를 제대로 불렀나)
    }

    @Test
    @DisplayName("없으면 예외 삭제한다. (existsById 확인)")
    void 없으면_삭제안함() {
      // given
      given(repository.existsById(999L)).willReturn(false);

      // when then
      assertThatThrownBy(()->service.delete(999L))
          .isInstanceOf(ActivityNotFoundException.class);
      verify(repository, never()).delete(any());


    }
  }
  /// --------------------------------------------------------------------------------

  @Nested
  @DisplayName("저장된 내용 검증 - ArgumentCaptor")
  class SavedContent{

    @Test
    @DisplayName("dd")
    void 저장내용검증() {
      // given
      LearningActivity activity = sample();
      given(repository.findById(1L)).willReturn(Optional.of(activity));
      given(repository.save(any())).willReturn(activity);

      // when
      service.update(1L, new UpdateActivityRequest("바뀐 제목", Visibility.PUBLIC));
      // ArgumentCaptor는 행위를 검증하는 verify를 시행할 때 호출하는 메서드의 매개값을 붙잡아서 활용할 수 있게 해 준다.
      // update에서는 사실 ArgumentCaptor를 사용 할 필요 없음
      // 샘플데이터를 직접 생성해서 전달하기 때문에 똑같은 객체를 수정해서 확인하면 끝
      // create에서는 비교대상이 없기 때문에 전달된 값을 검증하기 위해 ArgumentCaptor가 필요함
      ArgumentCaptor<LearningActivity> captor = ArgumentCaptor.forClass(LearningActivity.class);
      // then
      verify(repository).save(captor.capture());
      assertThat(captor.getValue().getTitle()).isEqualTo("바뀐 제목");
    }
  }
  /// --------------------------------------------------------------------------------
  @Nested
  @DisplayName("생성 create - 내부에서 만든 엔티티를 ArgumentCaptor로 검증")
  class Create{

    @Test
    @DisplayName("요청을 매핑해 저장한다 - 저장되는 엔티티의 필드가 요청과 일치")
    void 요청을_매핑해_저장() {
        // given
      CreateActivityRequest request = new CreateActivityRequest(
          ActivityCategory.LECTURE, "새 강의", 45, Visibility.PUBLIC, null, null,
          "이강사", null, null
      );
      //willREturn은 리턴할 객체를 직접 명시해야함
      //create() 테스트할 때는 테스트 메소드 안에 리턴해줄 entity가 없음
      given(repository.save(any(LearningActivity.class))).willAnswer(i -> i.getArgument(0));
      // when
      service.create(request,null);
      ArgumentCaptor<LearningActivity> captor = ArgumentCaptor.forClass(LearningActivity.class);

      // then
      verify(repository).save(captor.capture());
      LearningActivity saved = captor.getValue();
      assertThat(saved.getTitle()).isEqualTo("새 강의");
      assertThat(saved.getCategory()).isEqualTo(ActivityCategory.LECTURE);
      assertThat(saved.getMinutes()).isEqualTo(45);
      assertThat(saved.getInstructorName()).isEqualTo("이강사");
    }
  }
  /// --------------------------------------------------------------------------------
  @Nested
  @DisplayName("void 협력자 다루기 - willThrow")
  class voidCollabolator{
    @Test
    @DisplayName("삭제 협력자가 실패하면 예외가 전파된다.")
    void 삭제_실패_전파() {
        // given
        given(repository.existsById(1L)).willReturn(true);
        //deleteById를 호출하면서 1L을 주면 예외를 일부러 발생시키겠다
        willThrow(new RuntimeException("DB오류")).given(repository).deleteById(1L); //deleteById는 void를 반환하기때문에 이렇게 작성

        // when then
        assertThatThrownBy(()->service.delete(1L)).isInstanceOf(RuntimeException.class);
        
    }
  }
}