package com.chuseok22.webdav.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class TransferResultDTO {
  private int successCount;
  private int totalCount;
  private List<String> failedFiles;

  public boolean isAllSuccess() {
    return successCount == totalCount;
  }

}
