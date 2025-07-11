package com.chuseok22.webdav.global.util;

import com.chuseok22.webdav.global.exception.CustomException;
import com.chuseok22.webdav.global.exception.ErrorCode;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class FileUtil {

  /**
   * 사용자 입력 경로 정규화
   * 1. 앞 뒤 공백 제거
   * 2. 백슬래시 ('\')를 슬래시('/')로 변환
   * 3. 중복 슬래시("//")를 단일 슬래시로 축소
   * 4. 루트를 나타내는 "/"는 빈 문자열로 변환
   * 5. 절대 경로로 변환: 선행 슬래시('/') 추가
   * 6. 불필요한 후행 슬래시('/') 제거
   *
   * @param path
   * @return
   */
  public static String normalizePath(String path) {
    if (path == null) {
      return "";
    }
    String p = path.trim().replace('\\', '/');
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
   * WebDAV 서버에 접근하기 위한 완전한 URL을 생성
   * 경로 구간을 UTF-8로 인코딩하여 특수문자 및 공백 처리
   *
   * @param baseUrl        WebDAV 서버의 BASE URL (ex: "https://example.com:5006/webdav/share")
   * @param normalizedPath normalizePath로 정규화된 경로 (선행 '/' 포함 또는 빈 문자열)
   * @return 전체 URL 문자열
   */
  public static String buildUrl(String baseUrl, String normalizedPath) {
    Objects.requireNonNull(baseUrl, "Base URL must not be null");

    if (normalizedPath == null || normalizedPath.isEmpty()) {
      return removeTrailingSlash(baseUrl);
    }

    try {
      // 기본 URL 끝의 슬래시 제거
      String base = removeTrailingSlash(baseUrl);

      // 경로를 개별 구성요소로 분리
      String[] pathParts = normalizedPath.split("/");
      StringBuilder encodedPath = new StringBuilder();

      // 로그 추가
      log.info("요청 경로 분해: {}", Arrays.toString(pathParts));

      for (String part : pathParts) {
        if (!part.isEmpty()) {
          String encodedPart;

          // RFC 3986 호환 인코딩 (URLEncoder는 폼 인코딩 방식을 사용함)
          encodedPart = URLEncoder.encode(part, "UTF-8")
              .replace("+", "%20")
              .replace("%2F", "/")
              .replace("%7E", "~");

          // 이미 완전히 인코딩된 부분은 다시 인코딩하지 않음
          if (part.matches(".*%[0-9A-Fa-f]{2}.*")) {
            System.out.println("이미 인코딩된 경로 부분 발견: " + part);
            // %로 시작하는 시퀀스가 유효한 URL 인코딩인지 확인
            if (isValidUrlEncoded(part)) {
              encodedPart = part;
            }
          }

          encodedPath.append("/").append(encodedPart);
          System.out.println("부분 경로 추가: " + encodedPart);
        }
      }

      String result = base + encodedPath.toString();
      log.info("최종 URL: {}", result);
      return result;

    } catch (UnsupportedEncodingException e) {
      log.error("FilUtil URL 인코딩중 오류 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.URL_ENCODE_ERROR);
    }
  }

  /**
   * 문자열이 유효한 URL 인코딩 형식인지 확인
   */
  private static boolean isValidUrlEncoded(String s) {
    // %XX 형식의 패턴이 있는지 확인
    Matcher m = Pattern.compile("%[0-9A-Fa-f]{2}").matcher(s);
    while (m.find()) {
      // 유효한 인코딩 패턴
      return true;
    }
    return false;
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
