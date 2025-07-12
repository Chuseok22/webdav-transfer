package com.chuseok22.webdav.global.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class FileUtil {

  // URL 인코딩 패턴 (예: %20, %E1 등)
  private static final Pattern ENCODED_SEGMENT_PATTERN = Pattern.compile("^(?:%[0-9A-Fa-f]{2})+$");

  /**
   * 1. rawPath 정규화 및 인코딩
   * 2. baseUrl과 결합하여 하나의 url로 반환
   *
   * @param baseUrl 기본 URL
   * @param rawPath 사용자 입력 Path
   * @return
   */
  public String buildNormalizedAndEncodedUrl(String baseUrl, String rawPath) {
    String normalizedPath = normalizePath(rawPath);
    String encodedAndNormalizedPath = encodeUTF8(normalizedPath);
    return combineBaseAndPath(baseUrl, encodedAndNormalizedPath);
  }

  /**
   * 사용자 입력 경로 정규화
   * 1. 앞 뒤 공백 제거
   * 2. 백슬래시 ('\')를 슬래시('/')로 변환
   * 3. 중복 슬래시("//")를 단일 슬래시로 축소
   * 4. 루트를 나타내는 "/"는 빈 문자열로 변환
   * 5. 절대 경로로 변환: 선행 슬래시('/') 추가
   * 6. 불필요한 후행 슬래시('/') 제거
   *
   * @param rawPath 사용자 입력 경로
   * @return 정규화된 경로 ("/webdav")
   */
  public static String normalizePath(String rawPath) {
    if (rawPath == null) {
      return "";
    }
    String p = rawPath.trim().replace('\\', '/');
    p = p.replaceAll("/+", "/");
    if ("/".equals(p)) {
      return "";
    }
    if (!p.startsWith("/")) {
      p = "/" + p;
    }
    if (p.endsWith("/") && p.length() > 1) {
      p = p.substring(0, p.length() - 1);
    }
    return p;
  }

  /**
   * BASE URL과 경로를 결합합니다.
   *
   * @param baseUrl WebDAV 서버의 베이스 URL (후행 슬래시 제거)
   * @param path    경로
   * @return 결합된 URL
   */
  public String combineBaseAndPath(String baseUrl, String path) {
    String base = removeTrailingSlash(baseUrl);
    if (path == null || path.isEmpty()) {
      return base;
    }
    if (!path.startsWith("/")) {
      return base + "/" + path;
    }
    return base + path;
  }

  /**
   * 입력 문자열을 UTF-8로 인코딩
   *
   * @param s 경로
   * @return 인코딩된 문자열
   */
  public static String encodeUTF8(String s) {
    if (s == null || s.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    String[] segments = s.split("/");
    for (String segment : segments) {
      if (segment.isEmpty()) {
        continue;
      }
      // 이미 인코딩된 부분은 재인코딩하지 않음
      if (isValidUrlEncoded(segment)) {
        sb.append("/").append(segment);
      } else {
        String part = URLEncoder.encode(segment, StandardCharsets.UTF_8)
            .replace("+", "%20");
        sb.append("/").append(part);
      }
    }
    return sb.toString();
  }

  /**
   * 문자열이 유효한 URL 인코딩 형식인지 확인
   */
  private static boolean isValidUrlEncoded(String s) {
    return ENCODED_SEGMENT_PATTERN.matcher(s).matches();
  }

  /**
   * URL 또는 경로 문자열의 끝에 있는 슬래시('/) 제거
   *
   * @param url 슬래시 제거 대상 문자열
   * @return 후행 슬래시가 제거된 문자열
   */
  public static String removeTrailingSlash(String url) {
    if (url == null || url.isEmpty()) {
      return url;
    }
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }
}
