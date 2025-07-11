package com.chuseok22.webdav.controller.api;

import com.chuseok22.webdav.dto.request.FolderTransferDTO;
import com.chuseok22.webdav.dto.request.MultipleFileTransferDTO;
import com.chuseok22.webdav.dto.request.SingleFileTransferDTO;
import com.chuseok22.webdav.dto.response.TransferResultDTO;
import com.chuseok22.webdav.dto.response.WebDavFileDTO;
import com.chuseok22.webdav.global.aop.log.LogMonitoringInvocation;
import com.chuseok22.webdav.service.FileTransferService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webdav")
@RequiredArgsConstructor
@Tag(name = "WebDAV API", description = "클라우드 -> NAS 파일 전송 API")
public class WebDavController {

  private final FileTransferService fileTransferService;

  @LogMonitoringInvocation
  @GetMapping("/files/cloud")
  public ResponseEntity<List<WebDavFileDTO>> getFiles(String path) {
    return ResponseEntity.ok(fileTransferService.listCloudFiles(path));
  }

  @LogMonitoringInvocation
  @GetMapping("/files/nas")
  public ResponseEntity<List<WebDavFileDTO>> getNasFiles(String path) {
    return ResponseEntity.ok(fileTransferService.listNasFiles(path));
  }

  @LogMonitoringInvocation
  @PostMapping("/transfer/single")
  public ResponseEntity<Boolean> transferSingleFile(@RequestBody SingleFileTransferDTO request) {
    return ResponseEntity.ok(fileTransferService.transferSingleFile(request));
  }

  @LogMonitoringInvocation
  @PostMapping("/transfer/multiple")
  public ResponseEntity<TransferResultDTO> transferMultipleFiles(@RequestBody MultipleFileTransferDTO request) {
    return ResponseEntity.ok(fileTransferService.transferMultipleFiles(request));
  }

  @LogMonitoringInvocation
  @PostMapping("/transfer/folder")
  public ResponseEntity<TransferResultDTO> transferFolder(@RequestBody FolderTransferDTO request) {
    return ResponseEntity.ok(fileTransferService.transferFolder(request));
  }
}
