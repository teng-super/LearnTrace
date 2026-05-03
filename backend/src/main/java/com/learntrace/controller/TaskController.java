package com.learntrace.controller;

import com.learntrace.common.Result;
import com.learntrace.dto.PageResult;
import com.learntrace.dto.Requests;
import com.learntrace.security.SecurityUser;
import com.learntrace.service.CoreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
  private final CoreService service;

  public TaskController(CoreService service) {
    this.service = service;
  }

  @GetMapping
  public Result<PageResult<com.learntrace.entity.Task>> list(@RequestParam(defaultValue = "1") long page,
                                                             @RequestParam(defaultValue = "20") long size,
                                                             @RequestParam(required = false) Long goalId,
                                                             @RequestParam(required = false) Integer weekNo,
                                                             @RequestParam(required = false) String status,
                                                             @RequestParam(required = false) String keyword) {
    return Result.ok(service.taskList(SecurityUser.id(), page, size, goalId, weekNo, status, keyword));
  }

  @PostMapping
  public Result<com.learntrace.entity.Task> create(@Valid @RequestBody Requests.TaskDraft req) {
    return Result.ok(service.createTask(SecurityUser.id(), req));
  }

  @PutMapping("/{id}")
  public Result<com.learntrace.entity.Task> update(@PathVariable Long id, @Valid @RequestBody Requests.TaskDraft req) {
    return Result.ok(service.updateTask(SecurityUser.id(), id, req));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    service.deleteTask(SecurityUser.id(), id);
    return Result.ok();
  }

  @PatchMapping("/{id}/done")
  public Result<com.learntrace.entity.Task> done(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean done) {
    return Result.ok(service.setTaskDone(SecurityUser.id(), id, done));
  }

  @GetMapping("/today")
  public Result<List<com.learntrace.entity.Task>> today() {
    return Result.ok(service.todayTasks(SecurityUser.id()));
  }

  @GetMapping("/unfinished")
  public Result<List<com.learntrace.entity.Task>> unfinished() {
    return Result.ok(service.unfinishedTasks(SecurityUser.id()));
  }

  @PostMapping("/import-default-plan")
  public Result<com.learntrace.entity.Goal> importDefaultPlan() {
    return Result.ok(service.importDefaultPlan(SecurityUser.id()));
  }
}
