package com.learntrace.controller;

import com.learntrace.common.Result;
import com.learntrace.dto.Requests;
import com.learntrace.security.SecurityUser;
import com.learntrace.service.CoreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final CoreService service;

  public UserController(CoreService service) {
    this.service = service;
  }

  @PutMapping("/me")
  public Result<Map<String, Object>> updateProfile(@RequestBody Requests.ProfileUpdate req) {
    return Result.ok(service.updateProfile(SecurityUser.id(), req));
  }

  @PutMapping("/me/password")
  public Result<Void> updatePassword(@Valid @RequestBody Requests.PasswordUpdate req) {
    service.updatePassword(SecurityUser.id(), req);
    return Result.ok();
  }
}
