package com.chuseok22.webdav.dto.request;

import lombok.Getter;

@Getter
public class SingleFileTransferDTO {
  private String cloudPath;
  private String serverPath;
  boolean overwrite;

}
