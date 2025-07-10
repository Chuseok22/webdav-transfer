package com.chuseok22.webdav.controller.api;

import com.chuseok22.webdav.dto.WebDavFileDTO;
import com.chuseok22.webdav.service.FileTransferService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webdav")
@RequiredArgsConstructor
@Tag(name = "WebDAV API", description = "클라우드 -> NAS 파일 전송 API")
public class WebDavController {

  private final FileTransferService fileTransferService;

  @GetMapping("/files/cloud")
  public ResponseEntity<List<WebDavFileDTO>> getFiles(String path) {
    return ResponseEntity.ok(fileTransferService.listCloudFiles(path));
  }

  @GetMapping("/files/nas")
  public ResponseEntity<List<WebDavFileDTO>> getNasFiles(String path) {
    return ResponseEntity.ok(fileTransferService.listNasFiles(path));
  }

  @PostMapping("/transfer")
  public ResponseEntity<Boolean> transferSingleFile(String cloudPath, String serverPath, boolean overwrite) {
    return ResponseEntity.ok(fileTransferService.transferSingleFile(cloudPath, serverPath, overwrite));
  }
}
