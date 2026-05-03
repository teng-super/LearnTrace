package com.learntrace.controller;

import com.learntrace.common.Result;
import com.learntrace.dto.Requests;
import com.learntrace.security.SecurityUser;
import com.learntrace.service.CoreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final CoreService service;

  public AuthController(CoreService service) {
    this.service = service;
  }

  @PostMapping("/register")
  public Result<Map<String, Object>> register(@Valid @RequestBody Requests.Register req) {
    return Result.ok(service.register(req));
  }

  @PostMapping("/login")
  public Result<Map<String, Object>> login(@Valid @RequestBody Requests.Login req) {
    return Result.ok(service.login(req));
  }

  @GetMapping("/me")
  public Result<Map<String, Object>> me() {
    return Result.ok(service.me(SecurityUser.id()));
  }
}
