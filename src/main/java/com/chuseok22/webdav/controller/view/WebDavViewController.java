package com.chuseok22.webdav.controller.view;

import com.chuseok22.webdav.dto.response.WebDavFileDTO;
import com.chuseok22.webdav.service.FileTransferService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

  @GetMapping("/transfer/single")
  public String transferPage(
      @RequestParam(value = "cloudPath", required = false) String cloudPath,
      @RequestParam(value = "serverPath", required = false, defaultValue = "") String serverPath,
      Model model) {

    model.addAttribute("cloudPath", cloudPath);
    model.addAttribute("serverPath", serverPath);

    return "webdav/transfer";
  }

  @GetMapping("/transfer/multiple")
  public String multipleTransferPage(
      @RequestParam("cloudPaths") String cloudPathsParam,
      @RequestParam("serverPath") String serverPath,
      Model model) {

    List<String> cloudPaths = Arrays.asList(cloudPathsParam.split(","));
    model.addAttribute("cloudPaths", cloudPaths);
    model.addAttribute("serverPath", serverPath);
    model.addAttribute("fileCount", cloudPaths.size());

    return "webdav/multiple-transfer";
  }

  @GetMapping("/transfer/folder")
  public String folderTransferPage(
      @RequestParam("cloudPath") String cloudPath,
      @RequestParam("serverPath") String serverPath,
      Model model) {

    model.addAttribute("cloudPath", cloudPath);
    model.addAttribute("serverPath", serverPath);

    return "webdav/folder-transfer";
  }
}