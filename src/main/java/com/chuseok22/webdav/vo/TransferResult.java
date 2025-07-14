package com.chuseok22.webdav.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransferResult {
  SUCCESS("성공"),
  FAIL("실패"),
  DUPLICATE("중복"),
  ;

  private final String property;
}
