package com.learntrace.controller;

import com.learntrace.common.Result;
import com.learntrace.security.SecurityUser;
import com.learntrace.service.CoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
  private final CoreService service;

  public StatisticsController(CoreService service) {
    this.service = service;
  }

  @GetMapping("/overview")
  public Result<Map<String, Object>> overview() {
    return Result.ok(service.overview(SecurityUser.id()));
  }

  @GetMapping("/error-types")
  public Result<List<Map<String, Object>>> errorTypes() {
    return Result.ok(service.errorTypeStats(SecurityUser.id()));
  }

  @GetMapping("/error-tags")
  public Result<List<Map<String, Object>>> errorTags() {
    return Result.ok(service.errorTagStats(SecurityUser.id()));
  }

  @GetMapping("/trends")
  public Result<Map<String, Object>> trends() {
    return Result.ok(service.trends(SecurityUser.id()));
  }
}
