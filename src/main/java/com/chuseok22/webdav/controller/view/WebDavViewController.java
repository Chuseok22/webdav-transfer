package com.chuseok22.webdav.controller.view;

import com.chuseok22.webdav.dto.WebDavFileDTO;
import com.chuseok22.webdav.service.FileTransferService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/webdav")
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

  @GetMapping("/transfer")
  public String transferPage(
      @RequestParam(value = "cloudPath", required = false) String cloudPath,
      @RequestParam(value = "serverPath", required = false, defaultValue = "") String serverPath,
      Model model) {

    model.addAttribute("cloudPath", cloudPath);
    model.addAttribute("serverPath", serverPath);

    return "webdav/transfer";
  }
}