package com.learntrace.controller;

import com.learntrace.common.Result;
import com.learntrace.dto.PageResult;
import com.learntrace.dto.Requests;
import com.learntrace.entity.Goal;
import com.learntrace.entity.GoalTemplate;
import com.learntrace.entity.PlanTemplate;
import com.learntrace.entity.PlanTemplateTask;
import com.learntrace.security.SecurityUser;
import com.learntrace.service.CoreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GoalController {
  private final CoreService service;

  public GoalController(CoreService service) {
    this.service = service;
  }

  @GetMapping("/goal-templates")
  public Result<List<GoalTemplate>> goalTemplates() {
    return Result.ok(service.goalTemplates());
  }

  @GetMapping("/plan-templates")
  public Result<List<PlanTemplate>> planTemplates() {
    return Result.ok(service.planTemplates());
  }

  @GetMapping("/plan-templates/{id}/tasks")
  public Result<List<PlanTemplateTask>> planTemplateTasks(@PathVariable Long id) {
    return Result.ok(service.planTemplateTasks(id));
  }

  @GetMapping("/goals")
  public Result<PageResult<Goal>> list(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "20") long size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String status) {
    return Result.ok(service.goals(SecurityUser.id(), page, size, keyword, status));
  }

  @PostMapping("/goals")
  public Result<Goal> create(@Valid @RequestBody Requests.GoalUpsert req) {
    return Result.ok(service.createGoal(SecurityUser.id(), req));
  }

  @GetMapping("/goals/{id}")
  public Result<Map<String, Object>> detail(@PathVariable Long id) {
    return Result.ok(service.goalDetail(SecurityUser.id(), id));
  }

  @PutMapping("/goals/{id}")
  public Result<Goal> update(@PathVariable Long id, @Valid @RequestBody Requests.GoalUpsert req) {
    return Result.ok(service.updateGoal(SecurityUser.id(), id, req));
  }

  @DeleteMapping("/goals/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    service.deleteGoal(SecurityUser.id(), id);
    return Result.ok();
  }

  @PatchMapping("/goals/{id}/status")
  public Result<Goal> status(@PathVariable Long id, @Valid @RequestBody Requests.StatusPatch req) {
    return Result.ok(service.updateGoalStatus(SecurityUser.id(), id, req.status));
  }

  @PostMapping("/goals/import-template")
  public Result<Goal> importTemplate(@Valid @RequestBody Requests.ImportTemplate req) {
    return Result.ok(service.importGoalTemplate(SecurityUser.id(), req));
  }

  @PostMapping("/goals/custom-from-plan")
  public Result<Goal> customFromPlan(@RequestBody Requests.CustomFromPlan req) {
    return Result.ok(service.customFromPlan(SecurityUser.id(), req));
  }
}
