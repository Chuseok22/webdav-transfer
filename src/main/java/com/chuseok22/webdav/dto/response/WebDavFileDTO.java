package com.chuseok22.webdav.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class WebDavFileDTO {
  private String fileName;
  private String filePath;
  private long fileSize;
  private boolean isDirectory;
  private String lastModified;
}
