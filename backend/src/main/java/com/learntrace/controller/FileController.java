package com.learntrace.controller;

import com.learntrace.common.Result;
import com.learntrace.entity.FileAsset;
import com.learntrace.security.SecurityUser;
import com.learntrace.service.CoreService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FileController {
  private final CoreService service;

  public FileController(CoreService service) {
    this.service = service;
  }

  @PostMapping("/upload")
  public Result<FileAsset> upload(@RequestPart("file") MultipartFile file,
                                  @RequestParam(defaultValue = "GENERAL") String relationType,
                                  @RequestParam(required = false) Long relationId) throws IOException {
    return Result.ok(service.saveUpload(SecurityUser.id(), file, relationType, relationId));
  }

  @GetMapping("/public/{id}")
  public ResponseEntity<Resource> publicFile(@PathVariable Long id) {
    Path path = service.publicFilePath(id);
    return ResponseEntity.ok(new FileSystemResource(path));
  }
}
