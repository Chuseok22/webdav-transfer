package com.chuseok22.webdav.global.init;

import com.chuseok22.webdav.client.WebDavClient;
import com.chuseok22.webdav.global.exception.CustomException;
import com.chuseok22.webdav.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationRunner implements org.springframework.boot.ApplicationRunner {

  private final WebDavClient webDavClient;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if (!webDavClient.isCloudConnected()) {
      log.error("Cloud WebDAV 연결에 실패했습니다.");
      throw new CustomException(ErrorCode.CLOUD_CONNECTION_ERROR);
    }
    if (!webDavClient.isNasConnected()) {
      log.error("NAS WebDAV 연결에 실패했습니다.");
      throw new CustomException(ErrorCode.NAS_CONNECTION_ERROR);
    }
    log.info("Cloud 및 NAS WebDAV 연결 성공");
  }
}
