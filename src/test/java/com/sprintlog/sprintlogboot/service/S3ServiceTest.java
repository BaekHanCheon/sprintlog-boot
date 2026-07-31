package com.sprintlog.sprintlogboot.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import com.sprintlog.sprintlogboot.config.S3Config;
import com.sprintlog.sprintlogboot.config.S3Properties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@DisplayName("S3Service 테스트 (S3Mock)")
class S3ServiceTest {

  static final String BUCKET = "sprintlog-uploads";

  static S3MockContainer s3Mock; //진짜 S3처럼 행동한느 local server container
  static S3Service s3Service;
  static S3Client s3Client;

  @BeforeAll
  static void startMock() {
    // 로컬 가짜 S3 컨테이너 기동
    s3Mock = new S3MockContainer("latest");
    s3Mock.start();

    // 운영 코드와 *동일한* S3Config 로 클라이언트를 만든다(엔드포인트만 목으로).
    S3Properties props = new S3Properties();
    props.setBucket(BUCKET);
    props.setRegion("ap-northeast-2");
    props.setEndpoint(s3Mock.getHttpEndpoint());   // 목 주소
    props.setPresignMinutes(10);

    S3Config config = new S3Config();
    s3Client = config.s3Client(props);
    S3Presigner presigner = config.s3Presigner(props);
    s3Service = new S3Service(s3Client, presigner, props);

    // 버킷 생성(실제 배포 전엔 콘솔/IaC 로 만들지만, 테스트에선 SDK 로 준비)
    s3Client.createBucket(b -> b.bucket(BUCKET));
  }

  @AfterAll
  static void stopMock() {
    if (s3Mock != null) s3Mock.stop();
  }

  @Test
  @DisplayName("업로드 -> presigned URL로 실제 GET -> 같은 바인드가 돌아온다")
  void 엽로드후_presignedURL로_내려받는다() throws IOException, InterruptedException {
    // given
    byte[] content = "SprintLog S3 통합 테스트 이미지 바이트".getBytes(StandardCharsets.UTF_8);
    MockMultipartFile file = new MockMultipartFile("file", "proof.png", "image/png", content);

    // when then
    String key = s3Service.saveFile(file);
    assertThat(key).endsWith(".png");

    // presigned GET URL 생성
    String url = s3Service.getFileUrl(key);
    assertThat(url).contains(key).contains("X-Amz-Signature=");

    // 위에서 받은 URL로 실제 HTTP GET 요청 보내기
    HttpResponse<byte[]> response = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder(URI.create(url)).build(),
        HttpResponse.BodyHandlers.ofByteArray()
    );

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).isEqualTo(content);
  }

  @Test
  @DisplayName("허용되지 않는 확장자는 업로드가 거부된다.")
  void 허용안된_확장자_거부() {
    // given
    MockMultipartFile evil
        = new MockMultipartFile(
        "file", "malware.exe", "application/octet-stream", "x".getBytes()
    );

    // when & then
    assertThatThrownBy(() -> s3Service.saveFile(evil))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("허용되지 않는 파일");
  }

  @Test
  @DisplayName("삭제하면 객체가 사라진다.")
  void 삭제하면_사라진다() {
    // given
    byte[] content = "지울 파일".getBytes(StandardCharsets.UTF_8);
    MockMultipartFile file = new MockMultipartFile(
        "file", "temp.txt", "text/plain", content
    );

    // when
    String key = s3Service.saveFile(file);
    s3Service.deleteFile(key);

    // then
    // 삭제 후 s3Client로 직접 버킷에 접근해서 key값으로 Object를 꺼내려 하면 NoSuchKeyException이 발생할 것이다.
    assertThatThrownBy(() -> s3Client.getObject(b -> b.bucket(BUCKET).key(key)))
        .isInstanceOf(NoSuchKeyException.class);

  }




}

