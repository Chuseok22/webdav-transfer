package com.chuseok22.webdav.controller.view;

import com.chuseok22.webdav.dto.response.WebDavFileDTO;
import com.chuseok22.webdav.service.FileTransferService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class WebDavViewController {

  private final FileTransferService fileTransferService;

  @GetMapping("")
  public String mainPage() {
    return "webdav/main";
  }

  @GetMapping("/explorer")
  public String explorerPage(
      @RequestParam(value = "cloudPath", required = false, defaultValue = "") String cloudPath,
      @RequestParam(value = "serverPath", required = false, defaultValue = "") String serverPath,
      Model model) {

    List<WebDavFileDTO> cloudFiles = fileTransferService.listCloudFiles(cloudPath);
    List<WebDavFileDTO> nasFiles = fileTransferService.listNasFiles(serverPath);

    model.addAttribute("cloudFiles", cloudFiles);
    model.addAttribute("nasFiles", nasFiles);
    model.addAttribute("currentCloudPath", cloudPath);
    model.addAttribute("currentServerPath", serverPath);

    return "webdav/explorer";
  }

  @PostMapping("/transfer/single/submit")
  public String handleSingleTransferSubmit(
      @RequestParam("cloudPath") String cloudPath,
      @RequestParam("serverPath") String serverPath,
      Model model) {

    model.addAttribute("cloudPath", cloudPath);
    model.addAttribute("serverPath", serverPath);
    return "webdav/transfer";
  }

  @PostMapping("/transfer/multiple/submit")
  public String handleMultipleTransferSubmit(
      @RequestParam("cloudPaths") String cloudPathsJson,
      @RequestParam("serverPath") String serverPath,
      Model model) {

    // JSON 문자열을 리스트로 변환
    ObjectMapper mapper = new ObjectMapper();
    List<String> cloudPaths;
    try {
      cloudPaths = mapper.readValue(cloudPathsJson, new TypeReference<List<String>>() {
      });
    } catch (Exception e) {
      // 오류 처리
      return "redirect:/explorer";
    }

    model.addAttribute("cloudPaths", cloudPaths);
    model.addAttribute("serverPath", serverPath);
    model.addAttribute("fileCount", cloudPaths.size());
    return "webdav/multiple-transfer";
  }

  @PostMapping("/transfer/folder/submit")
  public String handleFolderTransferSubmit(
      @RequestParam("cloudPath") String cloudPath,
      @RequestParam("serverPath") String serverPath,
      Model model) {

    model.addAttribute("cloudPath", cloudPath);
    model.addAttribute("serverPath", serverPath);
    return "webdav/folder-transfer";
  }
}