package com.learntrace.controller;

import com.learntrace.common.Result;
import com.learntrace.security.SecurityUser;
import com.learntrace.service.CoreService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
  private final CoreService service;

  public DocumentController(CoreService service) {
    this.service = service;
  }

  @GetMapping("/{id}/editor-config")
  public Result<Map<String, Object>> editorConfig(@PathVariable Long id) {
    return Result.ok(service.onlyOfficeConfig(SecurityUser.id(), id));
  }

  @PostMapping("/onlyoffice/callback")
  public Map<String, Object> onlyOfficeCallback(@RequestBody Map<String, Object> body) {
    return service.onlyOfficeCallback(body);
  }
}
