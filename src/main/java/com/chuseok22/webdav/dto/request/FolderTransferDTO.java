package com.chuseok22.webdav.dto.request;

import lombok.Getter;

@Getter
public class FolderTransferDTO {

  private String folderPath;
  private String serverPath;
  private boolean overwrite;

}
