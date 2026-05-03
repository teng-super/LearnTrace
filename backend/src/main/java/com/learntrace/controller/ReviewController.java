package com.learntrace.controller;

import com.learntrace.common.Result;
import com.learntrace.dto.Requests;
import com.learntrace.entity.ReviewRecord;
import com.learntrace.security.SecurityUser;
import com.learntrace.service.CoreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
  private final CoreService service;

  public ReviewController(CoreService service) {
    this.service = service;
  }

  @GetMapping("/today")
  public Result<List<Map<String, Object>>> today() {
    return Result.ok(service.todayReviews(SecurityUser.id()));
  }

  @PostMapping("/{errorId}")
  public Result<ReviewRecord> review(@PathVariable Long errorId, @Valid @RequestBody Requests.ReviewSubmit req) {
    return Result.ok(service.review(SecurityUser.id(), errorId, req));
  }

  @GetMapping("/history/{errorId}")
  public Result<List<ReviewRecord>> history(@PathVariable Long errorId) {
    return Result.ok(service.reviewHistory(SecurityUser.id(), errorId));
  }
}
