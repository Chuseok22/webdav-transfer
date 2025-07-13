package com.chuseok22.webdav.client;

import com.chuseok22.webdav.dto.response.FolderItemDTO;
import com.chuseok22.webdav.dto.response.TransferResultDTO;
import com.chuseok22.webdav.dto.response.WebDavFileDTO;
import com.chuseok22.webdav.global.exception.CustomException;
import com.chuseok22.webdav.global.exception.ErrorCode;
import com.chuseok22.webdav.global.util.FileUtil;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
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
    return processFileTransfer(filePath, targetDir, overwrite);
  }

  /**
   * 다중 파일 전송 (클라우드 -> NAS)
   *
   * @param filePaths 전송할 파일 경로 목록
   * @param targetDir 대상 디렉토리
   * @param overwrite 덮어쓰기 여부
   * @return 성공한 파일 수와 전체 파일 수를 포함한 결과 객체
   */
  public TransferResultDTO transferMultipleFiles(List<String> filePaths, String targetDir, boolean overwrite) {
    int totalFiles = filePaths.size();
    int successCount = 0;
    List<String> failedFiles = new ArrayList<>();

    for (String filePath : filePaths) {
      boolean isSucceed = processFileTransfer(filePath, targetDir, overwrite);
      if (isSucceed) {
        successCount++;
        log.info("파일 전송 성공: {}/{}", successCount, totalFiles);
      } else {
        failedFiles.add(filePath);
      }
    }
    return TransferResultDTO.builder()
        .successCount(successCount)
        .totalCount(totalFiles)
        .failedFiles(failedFiles)
        .build();
  }

  /**
   * 폴더 전송 (클라우드 -> NAS)
   *
   * @param folderPath 전송할 폴더 경로
   * @param targetDir  대상 디렉토리
   * @param overwrite  덮어쓰기 여부
   * @return 전송 결과 객체
   */
  public TransferResultDTO transferFolder(String folderPath, String targetDir, boolean overwrite) {
    Sardine cloudClient = createClient(cloudUsername, cloudPassword);
    Sardine nasClient = createClient(nasUsername, nasPassword);
    log.info("폴더 전송을 시작합니다");
    log.info("클라우드 폴더 경로: {}", folderPath);
    log.info("NAS 대상 경로: {}", targetDir);

    String normalizedFolder = FileUtil.normalizePath(folderPath); // Cloud 폴더 경로 정규화
    log.info("Cloud 폴더 경로 정규화: {}", normalizedFolder);
    String normalizedTarget = FileUtil.normalizePath(targetDir); // NAS 타켓 경로 정규화
    log.info("NAS 대상 경로 정규화: {}", normalizedTarget);

    // 폴더명 추출
    String folderName = normalizedFolder.substring(normalizedFolder.lastIndexOf('/') + 1);
    log.info("폴더명 추출: {}", folderName);

    // 대상 폴더 경로 생성
    String targetFolderPath = normalizedTarget + "/" + folderName; // NAS에 폴더 경로 생성
    log.info("NAS에 생성할 폴더 경로: {}", targetFolderPath);
    String targetFolderUrl = FileUtil.buildNormalizedAndEncodedUrl(nasUrl, targetFolderPath); // 최종 NAS
    log.info("최종 NAS URL: {}", targetFolderUrl);

    List<String> failedFiles = new ArrayList<>();
    int totalFiles = 0;
    int successCount = 0;
    try {
      try {
        // 대상 폴더 생성
        if (!nasClient.exists(targetFolderUrl)) {
          log.debug("NAS에 대상 폴더가 존재하지 않아 폴더를 생성합니다: NAS경로: {}, 생성할 폴더: {}", targetDir, targetFolderUrl);
          nasClient.createDirectory(targetFolderUrl);
          log.info("NAS에 폴더 생성 성공: 생성된 폴더 경로: {}", targetFolderUrl);
        }
      } catch (Exception e) {
        log.error("폴더 생성 시 오류가 발생했습니다: {}, 오류: {}", targetFolderUrl, e.getMessage());
        throw new CustomException(ErrorCode.DIRECTORY_CREATE_ERROR);
      }

      // 모든 파일과 하위 폴더 조회 (재귀)
      log.info("모든 파일, 하위 폴더 조회 시작");
      List<FolderItemDTO> allItems = listFolderContentsRecursively(cloudClient, normalizedFolder, "");
      totalFiles = (int) allItems.stream().filter(item -> !item.isDirectory()).count();

      // 폴더 구조 생성
      for (FolderItemDTO item : allItems) {
        if (item.isDirectory()) {
          String newDirPath = targetFolderPath + "/" + item.getRelativePath();
          String newDirUrl = FileUtil.buildNormalizedAndEncodedUrl(nasUrl, newDirPath);
          if (!nasClient.exists(newDirUrl)) {
            nasClient.createDirectory(newDirUrl);
          }
        }
      }

      // 파일 전송
      for (FolderItemDTO item : allItems) {
        if (!item.isDirectory()) {
          try {
            String srcUrl = FileUtil.buildNormalizedAndEncodedUrl(cloudUrl, item.getFullPath());
            String dstPath = targetFolderPath + "/" + item.getRelativePath();
            String dstUrl = FileUtil.buildNormalizedAndEncodedUrl(nasUrl, dstPath);

            if (nasClient.exists(dstUrl) && !overwrite) {
              log.warn("파일 이미 존재(overwrite=false): {}", dstUrl);
              failedFiles.add(item.getFullPath());
              continue;
            }

            try (InputStream in = cloudClient.get(srcUrl)) {
              nasClient.put(dstUrl, in);
              successCount++;
              log.info("전송 성공: {}", item.getRelativePath());
            }
          } catch (IOException e) {
            log.error("파일 전송 실패 [{}]: {}", item.getFullPath(), e.getMessage());
            failedFiles.add(item.getFullPath());
          }
        }
      }
      return TransferResultDTO.builder()
          .successCount(successCount)
          .totalCount(totalFiles)
          .failedFiles(failedFiles)
          .build();
    } catch (IOException e) {
      log.error("폴더 전송 실패 [{}]: {}", folderPath, e.getMessage());
      throw new CustomException(ErrorCode.FILE_TRANSFER_ERROR);
    } finally {
      shutdownClient(cloudClient);
      shutdownClient(nasClient);
    }
  }

  /**
   * 파일 전송 로직 (클라우드 -> NAS)
   *
   * @param filePath  전송할 파일 경로 (ex. /home/무제.txt)
   * @param targetDir 대상 디렉토리 (ex. /home/folder)
   * @param overwrite 덮어쓰기 여부
   */
  public boolean processFileTransfer(String filePath, String targetDir, boolean overwrite) {
    Sardine cloudClient = createClient(cloudUsername, cloudPassword);
    Sardine nasClient = createClient(nasUsername, nasPassword);

    String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
    log.info("전송 요청된 파일명: {}", fileName);

    String cloudFilePathFullUrl = FileUtil.combineBaseAndPath(cloudUrl, filePath);
    String nasDirFullUrl = FileUtil.combineBaseAndPath(nasUrl, targetDir);
    log.info("파일 전송 요청: 파일명: {}, [{}] -> [{}]", fileName, cloudFilePathFullUrl, nasDirFullUrl);

    String cloudFilePathEncodedUrl = FileUtil.buildNormalizedAndEncodedUrl(cloudUrl, filePath);
    log.info("인코딩된 클라우드 파일 경로: {}", cloudFilePathEncodedUrl);
    String nasFilePathEncodedUrl = FileUtil.buildNormalizedAndEncodedUrl(nasDirFullUrl, fileName);
    log.info("인코딩된 NAS 파일 경로: {}", nasFilePathEncodedUrl);

    try {
      if (nasClient.exists(nasFilePathEncodedUrl) && !overwrite) {
        log.warn("파일: {} 이 이미 존재합니다 (overwrite = false) 건너뜁니다", fileName);
        return false;
      }
      log.info("전송 시작: {} -> {}", cloudFilePathFullUrl, nasDirFullUrl);
      try (InputStream inputStream = cloudClient.get(cloudFilePathEncodedUrl)) {
        log.info("CloudClient 전송 성공: {}", cloudFilePathEncodedUrl);
        nasClient.put(nasFilePathEncodedUrl, inputStream);
      }
      log.info("파일 전송 성공: {}", FileUtil.combineBaseAndPath(nasDirFullUrl, fileName));
      return true;
    } catch (IOException e) {
      log.error("전송 실패 [{} → {}]", cloudFilePathFullUrl, nasDirFullUrl, e);
      throw new CustomException(ErrorCode.FILE_TRANSFER_ERROR);
    } finally {
      shutdownClient(cloudClient);
      shutdownClient(nasClient);
    }
  }

  /**
   * 폴더 내용을 재귀적으로 조회
   */
  private List<FolderItemDTO> listFolderContentsRecursively(Sardine client, String folderPath, String relativePath) {
    List<FolderItemDTO> result = new ArrayList<>();
    String fullUrl = FileUtil.buildNormalizedAndEncodedUrl(cloudUrl, folderPath);
    log.info("폴더 재귀적 조회: 클라우드 URL: {}", fullUrl);
    log.info("folderPath: {}", folderPath);

    List<DavResource> resources;
    try {
      log.info("폴더 내용 조회 시도: {}", fullUrl);
      resources = client.list(fullUrl);
    } catch (IOException e) {
      log.error("DavResource 추출에 실패했습니다. 요청URL: {}", fullUrl);
      throw new CustomException(ErrorCode.DIRECTORY_READ_ERROR);
    }
    for (DavResource resource : resources) {
      // 현재 폴더 자체는 건너 뜀
      if (FileUtil.buildNormalizedAndEncodedUrl(cloudUrl, resource.getHref().toString()).equals(fullUrl)) {
        log.info("현재 폴더 자체는 건너뜁니다. 현재 폴더: {}, 요청 URL: {}", resource.getHref().toString(), fullUrl);
        continue;
      }

      String name = resource.getName();
      String newRelativePath = relativePath.isEmpty() ? name : relativePath + "/" + name;
      String fullPath = folderPath + "/" + name;

      FolderItemDTO folderItemDTO = FolderItemDTO.builder()
          .fileName(name)
          .relativePath(newRelativePath)
          .fullPath(fullPath)
          .isDirectory(resource.isDirectory())
          .build();
      result.add(folderItemDTO);

      // 디렉토리면 재귀 호출
      if (resource.isDirectory()) {
        log.info("하위 폴더 조회를 진행합니다");
        try {
          result.addAll(listFolderContentsRecursively(client, fullPath, newRelativePath));
        } catch (Exception e) {
          log.error("하위 폴더 조회 실패: {}, 원인: {}", fullPath, e.getMessage());
          throw new CustomException(ErrorCode.DIRECTORY_READ_ERROR);
        }
      }
    }
    return result;
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
      log.info("목록 조회 요청 URL: {}", FileUtil.combineBaseAndPath(baseUrl, rawPath));
      String fullEncodedUrl = FileUtil.buildNormalizedAndEncodedUrl(baseUrl, rawPath);

      return client.list(fullEncodedUrl).stream()
          .filter(r -> !FileUtil.normalizePath(r.getHref().toString()).equals(FileUtil.encodePathSegments(rawPath)))
          .sorted(Comparator.comparing(DavResource::getName))
          .map(r -> toDto(r, FileUtil.normalizePath(rawPath)))
          .collect(Collectors.toList());
    } catch (IOException e) {
      log.error("목록 조회 실패 [{}]", rawPath, e);
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

  private WebDavFileDTO toDto(DavResource resource, String parentPath) {
    String fileName = resource.getName();
    String path = FileUtil.combineBaseAndPath(parentPath, fileName);
    return WebDavFileDTO.builder()
        .fileName(fileName)
        .filePath(path)
        .fileSize(resource.getContentLength() / 1024)
        .isDirectory(resource.isDirectory())
        .lastModified(resource.getModified() != null ? DATE_FORMAT.format(resource.getModified()) : "")
        .build();
  }
}
