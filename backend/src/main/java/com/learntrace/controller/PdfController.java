package com.learntrace.controller;

import com.learntrace.common.Result;
import com.learntrace.dto.Requests;
import com.learntrace.entity.PdfAnnotation;
import com.learntrace.entity.StudyNote;
import com.learntrace.security.SecurityUser;
import com.learntrace.service.CoreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pdfs")
public class PdfController {
  private final CoreService service;

  public PdfController(CoreService service) {
    this.service = service;
  }

  @GetMapping("/{id}/annotations")
  public Result<List<PdfAnnotation>> annotations(@PathVariable Long id) {
    return Result.ok(service.pdfAnnotations(SecurityUser.id(), id));
  }

  @PostMapping("/{id}/annotations")
  public Result<PdfAnnotation> createAnnotation(@PathVariable Long id, @Valid @RequestBody Requests.PdfAnnotationCreate req) {
    return Result.ok(service.createPdfAnnotation(SecurityUser.id(), id, req));
  }

  @PostMapping("/{id}/excerpt")
  public Result<StudyNote> excerpt(@PathVariable Long id, @RequestBody Requests.PdfExcerpt req) {
    return Result.ok(service.excerptPdf(SecurityUser.id(), id, req));
  }
}
