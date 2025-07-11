package com.chuseok22.webdav.dto.request;

import java.util.List;
import lombok.Getter;

@Getter
public class MultipleFileTransferDTO {

  private List<String> filePaths;
  private String serverPath;
  private boolean overwrite;

}
