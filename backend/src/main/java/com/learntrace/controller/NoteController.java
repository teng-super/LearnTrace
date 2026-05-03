package com.learntrace.controller;

import com.learntrace.common.Result;
import com.learntrace.dto.PageResult;
import com.learntrace.dto.Requests;
import com.learntrace.entity.StudyNote;
import com.learntrace.security.SecurityUser;
import com.learntrace.service.CoreService;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
  private final CoreService service;

  public NoteController(CoreService service) {
    this.service = service;
  }

  @GetMapping
  public Result<PageResult<StudyNote>> list(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "20") long size,
                                            @RequestParam(required = false) Long goalId,
                                            @RequestParam(required = false) String keyword) {
    return Result.ok(service.noteList(SecurityUser.id(), page, size, goalId, keyword));
  }

  @PostMapping
  public Result<StudyNote> create(@Valid @RequestBody Requests.NoteUpsert req) {
    return Result.ok(service.createNote(SecurityUser.id(), req));
  }

  @PutMapping("/{id}")
  public Result<StudyNote> update(@PathVariable Long id, @Valid @RequestBody Requests.NoteUpsert req) {
    return Result.ok(service.updateNote(SecurityUser.id(), id, req));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    service.deleteNote(SecurityUser.id(), id);
    return Result.ok();
  }

  @PostMapping("/import")
  public Result<StudyNote> importNote(@RequestPart("file") MultipartFile file,
                                      @RequestParam(required = false) Long goalId) throws IOException {
    return Result.ok(service.importNote(SecurityUser.id(), file, goalId));
  }

  @GetMapping("/{id}/export/markdown")
  public ResponseEntity<Resource> exportMarkdown(@PathVariable Long id) throws IOException {
    Path path = service.exportNoteMarkdown(SecurityUser.id(), id);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"")
        .body(new FileSystemResource(path));
  }

  @GetMapping("/{id}/export/pdf")
  public ResponseEntity<Resource> exportPdf(@PathVariable Long id) throws IOException {
    Path path = service.exportNotePdf(SecurityUser.id(), id);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"")
        .body(new FileSystemResource(path));
  }
}
