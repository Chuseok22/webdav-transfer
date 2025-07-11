package com.chuseok22.webdav.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  // GLOBAL

  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다."),

  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

  ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),

  // INIT

  CLOUD_CONNECTION_ERROR(HttpStatus.BAD_GATEWAY, "클라우드 WebDAV 연결에 실패했습니다."),

  NAS_CONNECTION_ERROR(HttpStatus.BAD_GATEWAY, "NAS WebDAV 연결에 실패했습니다."),

  // READ

  DIRECTORY_READ_ERROR(HttpStatus.BAD_REQUEST, "파일 및 디렉터리 목록 조회에 실패했습니다."),

  // TRANSFER

  FILE_TRANSFER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 전송에 실패했습니다."),

  DIRECTORY_CREATE_ERROR(HttpStatus.UNAUTHORIZED, "NAS 폴더 생성 시 오류가 발생했습니다."),

  // URL

  URL_FORMAT_ERROR(HttpStatus.BAD_REQUEST, "URL 형식이 잘못되었습니다."),

  URL_ENCODE_ERROR(HttpStatus.BAD_REQUEST, "URL 인코딩 시 오류가 발생했습니다"),
  ;

  private final HttpStatus status;
  private final String message;
}
