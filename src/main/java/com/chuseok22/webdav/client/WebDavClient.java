package com.chuseok22.webdav.client;

import com.chuseok22.webdav.dto.WebDavFileDTO;
import com.chuseok22.webdav.global.exception.CustomException;
import com.chuseok22.webdav.global.exception.ErrorCode;
import com.chuseok22.webdav.global.util.FileUtil;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebDavClient {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @Value("${webdav.cloud.url}")
  private String cloudUrl;
  @Value("${webdav.cloud.username}")
  private String cloudUsername;
  @Value("${webdav.cloud.password}")
  private String cloudPassword;

  @Value("${webdav.nas.url}")
  private String nasUrl;
  @Value("${webdav.nas.username}")
  private String nasUsername;
  @Value("${webdav.nas.password}")
  private String nasPassword;

  /**
   * 클라우드에서 지정 경로의 파일/폴더 목록 조회
   */
  public List<WebDavFileDTO> listCloudFiles(String remotePath) {
    return listFiles(cloudUsername, cloudPassword, cloudUrl, remotePath);
  }

  /**
   * NAS에서 지정 경로의 파일/폴더 목록 조회
   */
  public List<WebDavFileDTO> listNasFiles(String remotePath) {
    return listFiles(nasUsername, nasPassword, nasUrl, remotePath);
  }

  /**
   * 단일 파일 전송 (클라우드 → NAS)
   *
   * @param filePath  전송할 파일 경로
   * @param targetDir 대상 디렉토리
   * @param overwrite 덮어쓰기 여부
   */
  public boolean transferFile(String filePath, String targetDir, boolean overwrite) {
    Sardine cloudClient = createClient(cloudUsername, cloudPassword);
    Sardine nasClient = createClient(nasUsername, nasPassword);

    String normalizePath = FileUtil.normalizePath(filePath);
    String normalizeDir = FileUtil.normalizePath(targetDir);
    String cloudFileUrl = FileUtil.buildUrl(cloudUrl, normalizePath);
    String nasFileUrl = FileUtil.buildUrl(nasUrl, normalizeDir);
    String fileName = normalizePath.substring(normalizePath.lastIndexOf('/') + 1);

    String dstUrl = nasFileUrl.endsWith("/") ? nasFileUrl + fileName : nasFileUrl + "/" + fileName;

    try {
      if (nasClient.exists(dstUrl) && !overwrite) {
        log.error("파일 이미 존재(overwrite=false): {}", dstUrl);
        return false;
      }
      log.info("전송 시작: {} → {}", cloudFileUrl, dstUrl);
      try (InputStream in = cloudClient.get(cloudFileUrl)) {
        nasClient.put(dstUrl, in);
      }
      log.info("전송 성공: {}", fileName);
      return true;
    } catch (IOException e) {
      log.error("전송 실패 [{} → {}]: {}", normalizePath, normalizeDir, e.getMessage());
      e.printStackTrace();
      throw new CustomException(ErrorCode.FILE_TRANSFER_ERROR);
    }
  }

  /**
   * 클라우드 WebDAV 연결 상태 확인
   */
  public boolean isCloudConnected() {
    return checkConnection(cloudUrl, cloudUsername, cloudPassword);
  }

  /**
   * NAS WebDAV 연결 상태 확인
   */
  public boolean isNasConnected() {
    return checkConnection(nasUrl, nasUsername, nasPassword);
  }

  private boolean checkConnection(String baseUrl, String user, String pass) {
    Sardine client = createClient(user, pass);
    try {
      client.list(baseUrl);
      return true;
    } catch (Exception e) {
      log.error("WebDAV 연결 실패 [{}]: {}", baseUrl, e.getMessage());
      return false;
    } finally {
      shutdownClient(client);
    }
  }

  /* --------------------- 공통 유틸 메서드 --------------------- */

  private List<WebDavFileDTO> listFiles(String username, String password, String baseUrl, String rawPath) {
    Sardine client = createClient(username, password);
    try {
      String path = FileUtil.normalizePath(rawPath);
      String url = FileUtil.buildUrl(baseUrl, path);
      log.info("목록 조회 URL: {}", url);
      return client.list(url).stream()
          .filter(r -> !r.getHref().toString().equals(url))
          .map(r -> toDto(r, path))
          .collect(Collectors.toList());
    } catch (IOException e) {
      log.error("목록 조회 실패 [{}]: {}", rawPath, e.getMessage());
      e.printStackTrace();
      throw new CustomException(ErrorCode.DIRECTORY_READ_ERROR);
    } finally {
      shutdownClient(client);
    }
  }

  private Sardine createClient(String user, String pass) {
    return SardineFactory.begin(user, pass);
  }

  private void shutdownClient(Sardine client) {
    try {
      client.shutdown();
    } catch (IOException e) {
      log.warn("Sardine 클라이언트 종료 실패", e);
    }
  }

  private WebDavFileDTO toDto(DavResource r, String parentPath) {
    String name = r.getName();
    String path = parentPath + (parentPath.endsWith("/") ? "" : "/") + name;
    return WebDavFileDTO.builder()
        .fileName(name)
        .filePath(path)
        .fileSize(r.getContentLength())
        .isDirectory(r.isDirectory())
        .lastModified(r.getModified() != null ? DATE_FORMAT.format(r.getModified()) : "")
        .build();
  }
}
