package com.chuseok22.webdav.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class FolderItemDTO {
  private String fileName;
  private String relativePath; // 상대 경로 (루트 폴더 기준)
  private String fullPath; // 전체 경로
  private boolean isDirectory;

}
