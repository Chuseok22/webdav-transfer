package com.chuseok22.webdav.global.util;

import com.chuseok22.webdav.global.exception.CustomException;
import com.chuseok22.webdav.global.exception.ErrorCode;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
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
    String base = removeTrailingSlash(baseUrl);
    String full = base + Optional.ofNullable(normalizedPath).orElse("");
    try {
      URL url = new URL(full);
      StringBuilder sb = new StringBuilder();
      String protocol = url.getProtocol();
      String host = url.getHost();
      int port = url.getPort();
      sb.append(protocol).append("://").append(host);
      if (port != -1) {
        sb.append(':').append(port);
      }
      for (String segment : url.getPath().split("/")) {
        if (segment.isEmpty()) {
          continue;
        }
        sb.append('/').append(URLEncoder.encode(segment, "UTF-8").replace("+", "%20"));
      }
      if (url.getQuery() != null) {
        sb.append('?').append(url.getQuery());
      }
      return sb.toString();
    } catch (MalformedURLException | UnsupportedEncodingException e) {
      throw new CustomException(ErrorCode.URL_FORMAT_ERROR);
    }
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
