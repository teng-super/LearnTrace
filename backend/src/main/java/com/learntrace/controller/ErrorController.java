package com.learntrace.controller;

import com.learntrace.common.Result;
import com.learntrace.dto.PageResult;
import com.learntrace.dto.Requests;
import com.learntrace.security.SecurityUser;
import com.learntrace.service.CoreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/errors")
public class ErrorController {
  private final CoreService service;

  public ErrorController(CoreService service) {
    this.service = service;
  }

  @GetMapping
  public Result<PageResult<Map<String, Object>>> list(@RequestParam(defaultValue = "1") long page,
                                                      @RequestParam(defaultValue = "20") long size,
                                                      @RequestParam Map<String, String> filters) {
    return Result.ok(service.errorList(SecurityUser.id(), page, size, filters));
  }

  @PostMapping
  public Result<Map<String, Object>> create(@Valid @RequestBody Requests.ErrorUpsert req) {
    return Result.ok(service.createError(SecurityUser.id(), req));
  }

  @GetMapping("/{id}")
  public Result<Map<String, Object>> detail(@PathVariable Long id) {
    return Result.ok(service.errorDetail(SecurityUser.id(), id));
  }

  @PutMapping("/{id}")
  public Result<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody Requests.ErrorUpsert req) {
    return Result.ok(service.updateError(SecurityUser.id(), id, req));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    service.deleteError(SecurityUser.id(), id);
    return Result.ok();
  }
}
