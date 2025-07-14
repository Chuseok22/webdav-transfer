package com.chuseok22.webdav.service;

import com.chuseok22.webdav.client.WebDavClient;
import com.chuseok22.webdav.dto.request.FolderTransferDTO;
import com.chuseok22.webdav.dto.request.MultipleFileTransferDTO;
import com.chuseok22.webdav.dto.request.SingleFileTransferDTO;
import com.chuseok22.webdav.dto.response.TransferResultDTO;
import com.chuseok22.webdav.dto.response.WebDavFileDTO;
import java.io.IOException;
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
   * 단일 파일 전송
   */
  public boolean transferSingleFile(SingleFileTransferDTO request) {
    return webDavClient.transferFile(request.getCloudPath(), request.getServerPath(), request.isOverwrite());
  }

  /**
   * 다중 파일 전송
   */
  public TransferResultDTO transferMultipleFiles(MultipleFileTransferDTO request) {
    return webDavClient.transferMultipleFiles(request.getCloudPaths(), request.getServerPath(), request.isOverwrite());
  }

  /**
   * 폴더 전송
   */
  public TransferResultDTO transferFolder(FolderTransferDTO request) throws IOException {
    return webDavClient.transferFolder(request.getFolderPath(), request.getServerPath(), request.isOverwrite());
  }
}
