package com.chuseok22.webdav.service;

import com.chuseok22.webdav.client.WebDavClient;
import com.chuseok22.webdav.dto.WebDavFileDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileTransferService {

  private final WebDavClient webDavClient;

  /**
   * 클라우드 파일 조회
   */
  public List<WebDavFileDTO> listCloudFiles(String remotePath) {
    return webDavClient.listCloudFiles(remotePath);
  }

  /**
   * NAS 파일 조회
   */
  public List<WebDavFileDTO> listNasFiles(String remotePath) {
    return webDavClient.listNasFiles(remotePath);
  }

  /**
   * 선택한 파일들을 클라우드에서 서버로 전송
   */

  /**
   * 단일 파일 전송
   */
  public boolean transferSingleFile(String cloudPath, String serverPath, boolean overwrite) {
    return webDavClient.transferFile(cloudPath, serverPath, overwrite);
  }
}
