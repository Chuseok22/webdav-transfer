package com.chuseok22.webdav.global.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

  private ErrorCode errorCode;
  private String errorMessage;
}
