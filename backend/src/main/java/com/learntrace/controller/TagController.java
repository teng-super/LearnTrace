package com.learntrace.controller;

import com.learntrace.common.Result;
import com.learntrace.dto.Requests;
import com.learntrace.entity.Tag;
import com.learntrace.security.SecurityUser;
import com.learntrace.service.CoreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
  private final CoreService service;

  public TagController(CoreService service) {
    this.service = service;
  }

  @GetMapping
  public Result<List<Tag>> list() {
    return Result.ok(service.tagList(SecurityUser.id()));
  }

  @PostMapping
  public Result<Tag> create(@Valid @RequestBody Requests.TagCreate req) {
    return Result.ok(service.createTag(SecurityUser.id(), req));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    service.deleteTag(SecurityUser.id(), id);
    return Result.ok();
  }
}
