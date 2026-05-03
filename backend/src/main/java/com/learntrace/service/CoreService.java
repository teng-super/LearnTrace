package com.learntrace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learntrace.common.BizException;
import com.learntrace.dto.PageResult;
import com.learntrace.dto.Requests;
import com.learntrace.entity.*;
import com.learntrace.mapper.*;
import com.learntrace.security.JwtUtil;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoreService {
  private final UserMapper users;
  private final GoalMapper goals;
  private final GoalStageMapper stages;
  private final TaskMapper tasks;
  private final ErrorLogMapper errors;
  private final TagMapper tags;
  private final ErrorLogTagMapper errorTags;
  private final ReviewRecordMapper reviews;
  private final PlanTemplateMapper planTemplates;
  private final PlanTemplateTaskMapper planTemplateTasks;
  private final GoalTemplateMapper goalTemplates;
  private final GoalTemplateStageMapper goalTemplateStages;
  private final StudyNoteMapper notes;
  private final FileAssetMapper files;
  private final PdfAnnotationMapper pdfAnnotations;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final Path uploadsDir;
  private final Path exportsDir;
  private final String publicBaseUrl;
  private final String onlyOfficeUrl;

  public CoreService(UserMapper users, GoalMapper goals, GoalStageMapper stages, TaskMapper tasks,
                     ErrorLogMapper errors, TagMapper tags, ErrorLogTagMapper errorTags,
                     ReviewRecordMapper reviews, PlanTemplateMapper planTemplates,
                     PlanTemplateTaskMapper planTemplateTasks, GoalTemplateMapper goalTemplates,
                     GoalTemplateStageMapper goalTemplateStages, StudyNoteMapper notes,
                     FileAssetMapper files, PdfAnnotationMapper pdfAnnotations,
                     PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                     @Value("${learntrace.storage.uploads-dir}") String uploadsDir,
                     @Value("${learntrace.storage.exports-dir}") String exportsDir,
                     @Value("${learntrace.storage.public-base-url}") String publicBaseUrl,
                     @Value("${learntrace.onlyoffice.document-server-url}") String onlyOfficeUrl) {
    this.users = users;
    this.goals = goals;
    this.stages = stages;
    this.tasks = tasks;
    this.errors = errors;
    this.tags = tags;
    this.errorTags = errorTags;
    this.reviews = reviews;
    this.planTemplates = planTemplates;
    this.planTemplateTasks = planTemplateTasks;
    this.goalTemplates = goalTemplates;
    this.goalTemplateStages = goalTemplateStages;
    this.notes = notes;
    this.files = files;
    this.pdfAnnotations = pdfAnnotations;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.uploadsDir = Path.of(uploadsDir);
    this.exportsDir = Path.of(exportsDir);
    this.publicBaseUrl = publicBaseUrl;
    this.onlyOfficeUrl = onlyOfficeUrl;
  }

  @Transactional
  public Map<String, Object> register(Requests.Register req) {
    if (users.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, req.username)) > 0) {
      throw new BizException("用户名已存在");
    }
    if (users.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, req.email)) > 0) {
      throw new BizException("邮箱已存在");
    }
    User user = new User();
    user.setUsername(req.username);
    user.setEmail(req.email);
    user.setPasswordHash(passwordEncoder.encode(req.password));
    user.setNickname(Optional.ofNullable(req.nickname).filter(s -> !s.isBlank()).orElse(req.username));
    user.setTheme("battle-dark");
    users.insert(user);
    return loginToken(user);
  }

  public Map<String, Object> login(Requests.Login req) {
    User user = users.selectOne(new LambdaQueryWrapper<User>()
        .eq(User::getUsername, req.usernameOrEmail)
        .or()
        .eq(User::getEmail, req.usernameOrEmail));
    if (user == null || !passwordEncoder.matches(req.password, user.getPasswordHash())) {
      throw new BizException(401, "账号或密码错误");
    }
    return loginToken(user);
  }

  public Map<String, Object> me(Long userId) {
    return sanitizeUser(requiredUser(userId));
  }

  public Map<String, Object> updateProfile(Long userId, Requests.ProfileUpdate req) {
    User user = requiredUser(userId);
    if (req.nickname != null) user.setNickname(req.nickname);
    if (req.avatarUrl != null) user.setAvatarUrl(req.avatarUrl);
    if (req.theme != null) user.setTheme(req.theme);
    user.setUpdatedAt(LocalDateTime.now());
    users.updateById(user);
    return sanitizeUser(user);
  }

  public void updatePassword(Long userId, Requests.PasswordUpdate req) {
    User user = requiredUser(userId);
    if (!passwordEncoder.matches(req.oldPassword, user.getPasswordHash())) {
      throw new BizException("旧密码不正确");
    }
    user.setPasswordHash(passwordEncoder.encode(req.newPassword));
    user.setUpdatedAt(LocalDateTime.now());
    users.updateById(user);
  }

  public List<GoalTemplate> goalTemplates() {
    return goalTemplates.selectList(new LambdaQueryWrapper<GoalTemplate>().orderByAsc(GoalTemplate::getRecommendedOrder));
  }

  public List<PlanTemplate> planTemplates() {
    return planTemplates.selectList(new LambdaQueryWrapper<PlanTemplate>().orderByAsc(PlanTemplate::getId));
  }

  public List<PlanTemplateTask> planTemplateTasks(Long templateId) {
    return planTemplateTasks.selectList(new LambdaQueryWrapper<PlanTemplateTask>()
        .eq(PlanTemplateTask::getTemplateId, templateId)
        .orderByAsc(PlanTemplateTask::getSortOrder));
  }

  @Transactional
  public Goal importGoalTemplate(Long userId, Requests.ImportTemplate req) {
    GoalTemplate template = requiredGoalTemplate(req.templateId);
    LocalDate start = Optional.ofNullable(req.startDate).orElse(LocalDate.now());
    Goal goal = new Goal();
    goal.setUserId(userId);
    goal.setTitle(Optional.ofNullable(req.title).filter(s -> !s.isBlank()).orElse(template.getTitle()));
    goal.setDescription(template.getDescription());
    goal.setCategory(template.getCategory());
    goal.setPriority(Optional.ofNullable(req.priority).orElse(template.getPriority()));
    goal.setStatus("NOT_STARTED");
    goal.setStartDate(start);
    goal.setDeadline(start.plusWeeks(16));
    goal.setProgress(0);
    goals.insert(goal);
    List<GoalTemplateStage> sourceStages = goalTemplateStages.selectList(
        new LambdaQueryWrapper<GoalTemplateStage>()
            .eq(GoalTemplateStage::getTemplateId, template.getId())
            .orderByAsc(GoalTemplateStage::getStageOrder));
    for (GoalTemplateStage source : sourceStages) {
      GoalStage stage = new GoalStage();
      stage.setUserId(userId);
      stage.setGoalId(goal.getId());
      stage.setTitle(source.getTitle());
      stage.setDescription(source.getDescription());
      stage.setStageOrder(source.getStageOrder());
      stage.setStatus("NOT_STARTED");
      stage.setStartDate(start.plusWeeks(Math.max(0, source.getStageOrder() - 1)));
      stage.setDeadline(start.plusWeeks(source.getStageOrder()).minusDays(1));
      stages.insert(stage);
      com.learntrace.entity.Task task = new com.learntrace.entity.Task();
      task.setUserId(userId);
      task.setGoalId(goal.getId());
      task.setStageId(stage.getId());
      task.setWeekNo(source.getStageOrder());
      task.setTitle("完成阶段复盘：" + source.getTitle());
      task.setDescription(source.getDescription());
      task.setTaskType("CUSTOM");
      task.setStatus("TODO");
      task.setPriority(goal.getPriority());
      task.setDueDate(stage.getDeadline());
      tasks.insert(task);
    }
    return goal;
  }

  @Transactional
  public Goal importDefaultPlan(Long userId) {
    PlanTemplate template = planTemplates.selectOne(new LambdaQueryWrapper<PlanTemplate>().eq(PlanTemplate::getCode, "HTML_8_WEEK_PLAN"));
    if (template == null) throw new BizException("默认八周计划模板未初始化");
    if (goals.selectCount(new LambdaQueryWrapper<Goal>().eq(Goal::getUserId, userId).eq(Goal::getTitle, "八周系统进阶作战图")) > 0) {
      throw new BizException("你已经导入过八周计划");
    }
    LocalDate start = LocalDate.now();
    Goal goal = new Goal();
    goal.setUserId(userId);
    goal.setTitle("八周系统进阶作战图");
    goal.setDescription("由原 study_plan.html 迁移而来，覆盖 Linux、C++ 编译链、并发、网络、FFmpeg 和学校三科。");
    goal.setCategory("音视频求职基础");
    goal.setPriority("HIGH");
    goal.setStatus("IN_PROGRESS");
    goal.setStartDate(start);
    goal.setDeadline(start.plusWeeks(8).minusDays(1));
    goal.setProgress(0);
    goals.insert(goal);

    Map<Integer, GoalStage> stageByWeek = new HashMap<>();
    for (int i = 1; i <= 8; i++) {
      GoalStage stage = new GoalStage();
      stage.setUserId(userId);
      stage.setGoalId(goal.getId());
      stage.setTitle("第 " + i + " 周");
      stage.setDescription("从默认八周计划导入");
      stage.setStageOrder(i);
      stage.setStatus("NOT_STARTED");
      stage.setStartDate(start.plusWeeks(i - 1L));
      stage.setDeadline(start.plusWeeks(i).minusDays(1));
      stages.insert(stage);
      stageByWeek.put(i, stage);
    }
    List<PlanTemplateTask> sourceTasks = planTemplateTasks.selectList(
        new LambdaQueryWrapper<PlanTemplateTask>().eq(PlanTemplateTask::getTemplateId, template.getId()).orderByAsc(PlanTemplateTask::getSortOrder));
    for (PlanTemplateTask source : sourceTasks) {
      com.learntrace.entity.Task task = new com.learntrace.entity.Task();
      Integer weekNo = Optional.ofNullable(source.getWeekNo()).orElse(0);
      GoalStage stage = weekNo > 0 ? stageByWeek.get(weekNo) : null;
      task.setUserId(userId);
      task.setGoalId(goal.getId());
      task.setStageId(stage == null ? null : stage.getId());
      task.setWeekNo(weekNo);
      task.setTitle(source.getTitle());
      task.setDescription(source.getDescription());
      task.setTaskType(source.getTaskType());
      task.setStatus("TODO");
      task.setPriority("MEDIUM");
      task.setDueDate(weekNo > 0 ? start.plusWeeks(weekNo).minusDays(1) : null);
      tasks.insert(task);
    }
    recalcGoalProgress(userId, goal.getId());
    return goal;
  }

  @Transactional
  public Goal customFromPlan(Long userId, Requests.CustomFromPlan req) {
    Goal goal = new Goal();
    goal.setUserId(userId);
    goal.setTitle(Optional.ofNullable(req.title).filter(s -> !s.isBlank()).orElse("自定义音视频学习目标"));
    goal.setDescription(req.description);
    goal.setCategory("自定义计划");
    goal.setPriority("HIGH");
    goal.setStatus("NOT_STARTED");
    goal.setStartDate(Optional.ofNullable(req.startDate).orElse(LocalDate.now()));
    goal.setDeadline(goal.getStartDate().plusWeeks(8).minusDays(1));
    goal.setProgress(0);
    goals.insert(goal);
    if (req.planTaskIds != null && !req.planTaskIds.isEmpty()) {
      List<PlanTemplateTask> selected = planTemplateTasks.selectBatchIds(req.planTaskIds);
      for (PlanTemplateTask source : selected) {
        com.learntrace.entity.Task task = new com.learntrace.entity.Task();
        task.setUserId(userId);
        task.setGoalId(goal.getId());
        task.setWeekNo(source.getWeekNo());
        task.setTitle(source.getTitle());
        task.setDescription(source.getDescription());
        task.setTaskType(source.getTaskType());
        task.setStatus("TODO");
        task.setPriority("MEDIUM");
        task.setDueDate(source.getWeekNo() == null ? null : goal.getStartDate().plusWeeks(source.getWeekNo()).minusDays(1));
        tasks.insert(task);
      }
    }
    return goal;
  }

  public PageResult<Goal> goals(Long userId, long page, long size, String keyword, String status) {
    LambdaQueryWrapper<Goal> wrapper = new LambdaQueryWrapper<Goal>().eq(Goal::getUserId, userId)
        .like(keyword != null && !keyword.isBlank(), Goal::getTitle, keyword)
        .eq(status != null && !status.isBlank(), Goal::getStatus, status)
        .orderByDesc(Goal::getUpdatedAt);
    Page<Goal> result = goals.selectPage(Page.of(page, size), wrapper);
    return new PageResult<>(result.getTotal(), page, size, result.getRecords());
  }

  public Map<String, Object> goalDetail(Long userId, Long id) {
    Goal goal = requiredGoal(userId, id);
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("goal", goal);
    data.put("stages", stages.selectList(new LambdaQueryWrapper<GoalStage>().eq(GoalStage::getUserId, userId).eq(GoalStage::getGoalId, id).orderByAsc(GoalStage::getStageOrder)));
    data.put("tasks", tasks.selectList(new LambdaQueryWrapper<com.learntrace.entity.Task>().eq(com.learntrace.entity.Task::getUserId, userId).eq(com.learntrace.entity.Task::getGoalId, id).orderByAsc(com.learntrace.entity.Task::getDueDate)));
    data.put("errors", errors.selectList(new LambdaQueryWrapper<ErrorLog>().eq(ErrorLog::getUserId, userId).eq(ErrorLog::getGoalId, id).orderByDesc(ErrorLog::getUpdatedAt).last("limit 8")));
    data.put("notes", notes.selectList(new LambdaQueryWrapper<StudyNote>().eq(StudyNote::getUserId, userId).eq(StudyNote::getGoalId, id).orderByDesc(StudyNote::getUpdatedAt).last("limit 8")));
    return data;
  }

  @Transactional
  public Goal createGoal(Long userId, Requests.GoalUpsert req) {
    Goal goal = new Goal();
    BeanUtils.copyProperties(req, goal);
    goal.setUserId(userId);
    goal.setPriority(defaultString(goal.getPriority(), "MEDIUM"));
    goal.setStatus(defaultString(goal.getStatus(), "NOT_STARTED"));
    goal.setProgress(0);
    goals.insert(goal);
    upsertGoalChildren(userId, goal.getId(), req);
    return goal;
  }

  @Transactional
  public Goal updateGoal(Long userId, Long id, Requests.GoalUpsert req) {
    Goal goal = requiredGoal(userId, id);
    BeanUtils.copyProperties(req, goal);
    goal.setUpdatedAt(LocalDateTime.now());
    goals.updateById(goal);
    upsertGoalChildren(userId, id, req);
    recalcGoalProgress(userId, id);
    return goal;
  }

  public void deleteGoal(Long userId, Long id) {
    requiredGoal(userId, id);
    goals.deleteById(id);
  }

  public Goal updateGoalStatus(Long userId, Long id, String status) {
    Goal goal = requiredGoal(userId, id);
    goal.setStatus(status);
    goal.setUpdatedAt(LocalDateTime.now());
    goals.updateById(goal);
    return goal;
  }

  public PageResult<com.learntrace.entity.Task> taskList(Long userId, long page, long size, Long goalId, Integer weekNo, String status, String keyword) {
    LambdaQueryWrapper<com.learntrace.entity.Task> wrapper = new LambdaQueryWrapper<com.learntrace.entity.Task>()
        .eq(com.learntrace.entity.Task::getUserId, userId)
        .eq(goalId != null, com.learntrace.entity.Task::getGoalId, goalId)
        .eq(weekNo != null, com.learntrace.entity.Task::getWeekNo, weekNo)
        .eq(status != null && !status.isBlank(), com.learntrace.entity.Task::getStatus, status)
        .like(keyword != null && !keyword.isBlank(), com.learntrace.entity.Task::getTitle, keyword)
        .orderByAsc(com.learntrace.entity.Task::getDueDate);
    Page<com.learntrace.entity.Task> result = tasks.selectPage(Page.of(page, size), wrapper);
    return new PageResult<>(result.getTotal(), page, size, result.getRecords());
  }

  public List<com.learntrace.entity.Task> todayTasks(Long userId) {
    LocalDate today = LocalDate.now();
    return tasks.selectList(new LambdaQueryWrapper<com.learntrace.entity.Task>()
        .eq(com.learntrace.entity.Task::getUserId, userId)
        .le(com.learntrace.entity.Task::getDueDate, today)
        .ne(com.learntrace.entity.Task::getStatus, "DONE")
        .orderByAsc(com.learntrace.entity.Task::getDueDate));
  }

  public List<com.learntrace.entity.Task> unfinishedTasks(Long userId) {
    return tasks.selectList(new LambdaQueryWrapper<com.learntrace.entity.Task>()
        .eq(com.learntrace.entity.Task::getUserId, userId)
        .ne(com.learntrace.entity.Task::getStatus, "DONE")
        .orderByAsc(com.learntrace.entity.Task::getDueDate));
  }

  public com.learntrace.entity.Task createTask(Long userId, Requests.TaskDraft req) {
    com.learntrace.entity.Task task = new com.learntrace.entity.Task();
    BeanUtils.copyProperties(req, task);
    task.setUserId(userId);
    task.setStatus(defaultString(task.getStatus(), "TODO"));
    task.setPriority(defaultString(task.getPriority(), "MEDIUM"));
    task.setTaskType(defaultString(task.getTaskType(), "CUSTOM"));
    tasks.insert(task);
    if (task.getGoalId() != null) recalcGoalProgress(userId, task.getGoalId());
    return task;
  }

  public com.learntrace.entity.Task updateTask(Long userId, Long id, Requests.TaskDraft req) {
    com.learntrace.entity.Task task = requiredTask(userId, id);
    BeanUtils.copyProperties(req, task);
    task.setUpdatedAt(LocalDateTime.now());
    tasks.updateById(task);
    if (task.getGoalId() != null) recalcGoalProgress(userId, task.getGoalId());
    return task;
  }

  public void deleteTask(Long userId, Long id) {
    com.learntrace.entity.Task task = requiredTask(userId, id);
    tasks.deleteById(id);
    if (task.getGoalId() != null) recalcGoalProgress(userId, task.getGoalId());
  }

  public com.learntrace.entity.Task setTaskDone(Long userId, Long id, boolean done) {
    com.learntrace.entity.Task task = requiredTask(userId, id);
    task.setStatus(done ? "DONE" : "TODO");
    task.setCompletedAt(done ? LocalDateTime.now() : null);
    task.setUpdatedAt(LocalDateTime.now());
    tasks.updateById(task);
    if (task.getGoalId() != null) recalcGoalProgress(userId, task.getGoalId());
    return task;
  }

  public PageResult<Map<String, Object>> errorList(Long userId, long page, long size, Map<String, String> filters) {
    LambdaQueryWrapper<ErrorLog> wrapper = new LambdaQueryWrapper<ErrorLog>().eq(ErrorLog::getUserId, userId)
        .like(has(filters, "keyword"), ErrorLog::getTitle, filters.get("keyword"))
        .eq(has(filters, "errorType"), ErrorLog::getErrorType, filters.get("errorType"))
        .eq(has(filters, "severity"), ErrorLog::getSeverity, filters.get("severity"))
        .eq(has(filters, "status"), ErrorLog::getStatus, filters.get("status"))
        .eq(has(filters, "goalId"), ErrorLog::getGoalId, parseLong(filters.get("goalId")))
        .orderByDesc(ErrorLog::getUpdatedAt);
    Page<ErrorLog> result = errors.selectPage(Page.of(page, size), wrapper);
    List<Map<String, Object>> records = result.getRecords().stream().map(this::errorWithTags).toList();
    return new PageResult<>(result.getTotal(), page, size, records);
  }

  public Map<String, Object> errorDetail(Long userId, Long id) {
    ErrorLog error = requiredError(userId, id);
    Map<String, Object> data = errorWithTags(error);
    data.put("reviews", reviews.selectList(new LambdaQueryWrapper<ReviewRecord>().eq(ReviewRecord::getUserId, userId).eq(ReviewRecord::getErrorLogId, id).orderByDesc(ReviewRecord::getReviewedAt)));
    return data;
  }

  @Transactional
  public Map<String, Object> createError(Long userId, Requests.ErrorUpsert req) {
    ErrorLog error = new ErrorLog();
    BeanUtils.copyProperties(req, error);
    error.setUserId(userId);
    error.setErrorType(defaultString(error.getErrorType(), "OTHER"));
    error.setSeverity(defaultString(error.getSeverity(), "MEDIUM"));
    error.setStatus(defaultString(error.getStatus(), "UNRESOLVED"));
    error.setReviewCount(0);
    if (error.getNextReviewAt() == null) error.setNextReviewAt(LocalDateTime.now().plusDays(1));
    errors.insert(error);
    saveErrorTags(userId, error.getId(), req);
    return errorWithTags(requiredError(userId, error.getId()));
  }

  @Transactional
  public Map<String, Object> updateError(Long userId, Long id, Requests.ErrorUpsert req) {
    ErrorLog error = requiredError(userId, id);
    BeanUtils.copyProperties(req, error);
    error.setUpdatedAt(LocalDateTime.now());
    errors.updateById(error);
    saveErrorTags(userId, id, req);
    return errorWithTags(requiredError(userId, id));
  }

  public void deleteError(Long userId, Long id) {
    requiredError(userId, id);
    errorTags.delete(new LambdaQueryWrapper<ErrorLogTag>().eq(ErrorLogTag::getErrorLogId, id));
    errors.deleteById(id);
  }

  public List<Map<String, Object>> todayReviews(Long userId) {
    return errors.selectList(new LambdaQueryWrapper<ErrorLog>()
            .eq(ErrorLog::getUserId, userId)
            .le(ErrorLog::getNextReviewAt, LocalDateTime.now())
            .orderByAsc(ErrorLog::getNextReviewAt))
        .stream().map(this::errorWithTags).toList();
  }

  @Transactional
  public ReviewRecord review(Long userId, Long errorId, Requests.ReviewSubmit req) {
    ErrorLog error = requiredError(userId, errorId);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime next = switch (req.result) {
      case "MASTERED" -> now.plusDays(7);
      case "UNCERTAIN" -> now.plusDays(3);
      case "FORGOT" -> now.plusDays(1);
      default -> throw new BizException("未知复习结果");
    };
    ReviewRecord record = new ReviewRecord();
    record.setUserId(userId);
    record.setErrorLogId(errorId);
    record.setResult(req.result);
    record.setNote(req.note);
    record.setReviewedAt(now);
    record.setNextReviewAt(next);
    reviews.insert(record);
    error.setReviewCount(Optional.ofNullable(error.getReviewCount()).orElse(0) + 1);
    error.setLastReviewedAt(now);
    error.setNextReviewAt(next);
    error.setStatus(req.result.equals("MASTERED") ? "RESOLVED" : "NEED_REVIEW");
    error.setUpdatedAt(now);
    errors.updateById(error);
    return record;
  }

  public List<ReviewRecord> reviewHistory(Long userId, Long errorId) {
    requiredError(userId, errorId);
    return reviews.selectList(new LambdaQueryWrapper<ReviewRecord>().eq(ReviewRecord::getUserId, userId).eq(ReviewRecord::getErrorLogId, errorId).orderByDesc(ReviewRecord::getReviewedAt));
  }

  public List<Tag> tagList(Long userId) {
    return tags.selectList(new LambdaQueryWrapper<Tag>().eq(Tag::getUserId, userId).orderByAsc(Tag::getName));
  }

  public Tag createTag(Long userId, Requests.TagCreate req) {
    Tag tag = ensureTag(userId, req.name, req.color);
    return tag;
  }

  public void deleteTag(Long userId, Long id) {
    Tag tag = tags.selectById(id);
    if (tag == null || !Objects.equals(tag.getUserId(), userId)) throw new BizException("标签不存在");
    errorTags.delete(new LambdaQueryWrapper<ErrorLogTag>().eq(ErrorLogTag::getTagId, id));
    tags.deleteById(id);
  }

  public Map<String, Object> overview(Long userId) {
    LocalDate today = LocalDate.now();
    LocalDateTime weekStart = today.minusDays(6).atStartOfDay();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("totalGoals", goals.selectCount(new LambdaQueryWrapper<Goal>().eq(Goal::getUserId, userId)));
    data.put("activeGoals", goals.selectCount(new LambdaQueryWrapper<Goal>().eq(Goal::getUserId, userId).eq(Goal::getStatus, "IN_PROGRESS")));
    data.put("completedGoals", goals.selectCount(new LambdaQueryWrapper<Goal>().eq(Goal::getUserId, userId).eq(Goal::getStatus, "COMPLETED")));
    data.put("totalTasks", tasks.selectCount(new LambdaQueryWrapper<com.learntrace.entity.Task>().eq(com.learntrace.entity.Task::getUserId, userId)));
    data.put("doneTasks", tasks.selectCount(new LambdaQueryWrapper<com.learntrace.entity.Task>().eq(com.learntrace.entity.Task::getUserId, userId).eq(com.learntrace.entity.Task::getStatus, "DONE")));
    data.put("todayTasks", tasks.selectCount(new LambdaQueryWrapper<com.learntrace.entity.Task>().eq(com.learntrace.entity.Task::getUserId, userId).le(com.learntrace.entity.Task::getDueDate, today).ne(com.learntrace.entity.Task::getStatus, "DONE")));
    data.put("totalErrors", errors.selectCount(new LambdaQueryWrapper<ErrorLog>().eq(ErrorLog::getUserId, userId)));
    data.put("resolvedErrors", errors.selectCount(new LambdaQueryWrapper<ErrorLog>().eq(ErrorLog::getUserId, userId).eq(ErrorLog::getStatus, "RESOLVED")));
    data.put("unresolvedErrors", errors.selectCount(new LambdaQueryWrapper<ErrorLog>().eq(ErrorLog::getUserId, userId).eq(ErrorLog::getStatus, "UNRESOLVED")));
    data.put("todayReviews", errors.selectCount(new LambdaQueryWrapper<ErrorLog>().eq(ErrorLog::getUserId, userId).le(ErrorLog::getNextReviewAt, LocalDateTime.now())));
    data.put("weekNewErrors", errors.selectCount(new LambdaQueryWrapper<ErrorLog>().eq(ErrorLog::getUserId, userId).ge(ErrorLog::getCreatedAt, weekStart)));
    data.put("currentGoal", goals.selectList(new LambdaQueryWrapper<Goal>().eq(Goal::getUserId, userId).eq(Goal::getStatus, "IN_PROGRESS").orderByAsc(Goal::getDeadline).last("limit 1")).stream().findFirst().orElse(null));
    data.put("todayTaskItems", todayTasks(userId).stream().limit(6).toList());
    data.put("reviewItems", todayReviews(userId).stream().limit(6).toList());
    data.put("recentErrors", errors.selectList(new LambdaQueryWrapper<ErrorLog>().eq(ErrorLog::getUserId, userId).orderByDesc(ErrorLog::getCreatedAt).last("limit 6")));
    return data;
  }

  public List<Map<String, Object>> errorTypeStats(Long userId) {
    return aggregate(userId, "error_type");
  }

  public List<Map<String, Object>> errorTagStats(Long userId) {
    List<Long> errorIds = errors.selectList(new LambdaQueryWrapper<ErrorLog>().eq(ErrorLog::getUserId, userId))
        .stream().map(ErrorLog::getId).toList();
    if (errorIds.isEmpty()) return List.of();
    Map<Long, Long> counts = errorTags.selectList(new LambdaQueryWrapper<ErrorLogTag>().in(ErrorLogTag::getErrorLogId, errorIds))
        .stream().collect(Collectors.groupingBy(ErrorLogTag::getTagId, Collectors.counting()));
    if (counts.isEmpty()) return List.of();
    Map<Long, Tag> tagMap = tags.selectBatchIds(counts.keySet()).stream().collect(Collectors.toMap(Tag::getId, t -> t));
    return counts.entrySet().stream()
        .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
        .limit(12)
        .map(entry -> {
          Tag tag = tagMap.get(entry.getKey());
          Map<String, Object> row = new LinkedHashMap<>();
          row.put("name", tag == null ? "未知标签" : tag.getName());
          row.put("value", entry.getValue());
          return row;
        })
        .toList();
  }

  public Map<String, Object> trends(Long userId) {
    LocalDate start = LocalDate.now().minusDays(6);
    List<String> days = new ArrayList<>();
    List<Long> done = new ArrayList<>();
    List<Long> errorNew = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      LocalDate day = start.plusDays(i);
      days.add(day.toString());
      done.add(tasks.selectCount(new LambdaQueryWrapper<com.learntrace.entity.Task>()
          .eq(com.learntrace.entity.Task::getUserId, userId)
          .ge(com.learntrace.entity.Task::getCompletedAt, day.atStartOfDay())
          .lt(com.learntrace.entity.Task::getCompletedAt, day.plusDays(1).atStartOfDay())));
      errorNew.add(errors.selectCount(new LambdaQueryWrapper<ErrorLog>()
          .eq(ErrorLog::getUserId, userId)
          .ge(ErrorLog::getCreatedAt, day.atStartOfDay())
          .lt(ErrorLog::getCreatedAt, day.plusDays(1).atStartOfDay())));
    }
    return Map.of("days", days, "doneTasks", done, "newErrors", errorNew);
  }

  public PageResult<StudyNote> noteList(Long userId, long page, long size, Long goalId, String keyword) {
    LambdaQueryWrapper<StudyNote> wrapper = new LambdaQueryWrapper<StudyNote>()
        .eq(StudyNote::getUserId, userId)
        .eq(goalId != null, StudyNote::getGoalId, goalId)
        .like(keyword != null && !keyword.isBlank(), StudyNote::getTitle, keyword)
        .orderByDesc(StudyNote::getUpdatedAt);
    Page<StudyNote> result = notes.selectPage(Page.of(page, size), wrapper);
    return new PageResult<>(result.getTotal(), page, size, result.getRecords());
  }

  public StudyNote createNote(Long userId, Requests.NoteUpsert req) {
    StudyNote note = new StudyNote();
    BeanUtils.copyProperties(req, note);
    note.setUserId(userId);
    note.setNoteType(defaultString(note.getNoteType(), "MARKDOWN"));
    notes.insert(note);
    return note;
  }

  public StudyNote updateNote(Long userId, Long id, Requests.NoteUpsert req) {
    StudyNote note = requiredNote(userId, id);
    BeanUtils.copyProperties(req, note);
    note.setUpdatedAt(LocalDateTime.now());
    notes.updateById(note);
    return note;
  }

  public void deleteNote(Long userId, Long id) {
    requiredNote(userId, id);
    notes.deleteById(id);
  }

  @Transactional
  public StudyNote importNote(Long userId, MultipartFile multipart, Long goalId) throws IOException {
    FileAsset asset = saveUpload(userId, multipart, "NOTE_SOURCE", goalId);
    String name = multipart.getOriginalFilename() == null ? "未命名笔记" : multipart.getOriginalFilename();
    String ext = extension(name).toLowerCase(Locale.ROOT);
    String markdown = switch (ext) {
      case "md", "markdown" -> Files.readString(Path.of(asset.getStoragePath()), StandardCharsets.UTF_8);
      case "pdf" -> "# " + stripExt(name) + "\n\n> 已导入 PDF 原件，可在 PDF 预览中批注并摘录到笔记。\n\n原文件：" + name;
      case "doc", "docx" -> "# " + stripExt(name) + "\n\n> 已导入 Word 原件，可通过 ONLYOFFICE 原格式编辑保存。\n\n原文件：" + name;
      default -> "# " + stripExt(name) + "\n\n> 已作为附件导入。";
    };
    StudyNote note = new StudyNote();
    note.setUserId(userId);
    note.setGoalId(goalId);
    note.setTitle(stripExt(name));
    note.setContentMarkdown(markdown);
    note.setNoteType(ext.equals("pdf") ? "PDF" : (ext.equals("doc") || ext.equals("docx") ? "WORD" : "MARKDOWN"));
    note.setSourceFileId(asset.getId());
    notes.insert(note);
    return note;
  }

  public FileAsset saveUpload(Long userId, MultipartFile multipart, String relationType, Long relationId) throws IOException {
    Files.createDirectories(uploadsDir);
    String original = Optional.ofNullable(multipart.getOriginalFilename()).orElse("upload.bin");
    String ext = extension(original);
    String stored = UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);
    Path target = uploadsDir.resolve(stored).normalize();
    multipart.transferTo(target);
    FileAsset asset = new FileAsset();
    asset.setUserId(userId);
    asset.setOriginalName(original);
    asset.setStoredName(stored);
    asset.setStoragePath(target.toString());
    asset.setFileType(ext.toUpperCase(Locale.ROOT));
    asset.setMimeType(multipart.getContentType());
    asset.setSizeBytes(multipart.getSize());
    asset.setRelationType(relationType);
    asset.setRelationId(relationId);
    files.insert(asset);
    return asset;
  }

  public Path publicFilePath(Long id) {
    FileAsset asset = files.selectById(id);
    if (asset == null) throw new BizException("文件不存在");
    return Path.of(asset.getStoragePath());
  }

  public Map<String, Object> onlyOfficeConfig(Long userId, Long fileId) {
    FileAsset asset = requiredFile(userId, fileId);
    return Map.of(
        "documentServerUrl", onlyOfficeUrl,
        "config", Map.of(
            "document", Map.of(
                "fileType", extension(asset.getOriginalName()),
                "key", asset.getId() + "-" + Optional.ofNullable(asset.getUpdatedAt()).orElse(asset.getCreatedAt()).toString().replace(":", "-"),
                "title", asset.getOriginalName(),
                "url", publicBaseUrl + "/" + asset.getId()),
            "documentType", "word",
            "editorConfig", Map.of(
                "mode", "edit",
                "lang", "zh-CN",
                "callbackUrl", "/api/documents/onlyoffice/callback")));
  }

  public Map<String, Object> onlyOfficeCallback(Map<String, Object> body) {
    Object status = body.get("status");
    Object url = body.get("url");
    Object key = body.get("key");
    if (url != null && key != null && (Objects.equals(status, 2) || Objects.equals(status, 6) || Objects.equals(String.valueOf(status), "2") || Objects.equals(String.valueOf(status), "6"))) {
      try {
        Long fileId = Long.valueOf(String.valueOf(key).split("-")[0]);
        FileAsset asset = files.selectById(fileId);
        if (asset != null) {
          HttpRequest request = HttpRequest.newBuilder(URI.create(String.valueOf(url))).GET().build();
          byte[] bytes = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
          Files.write(Path.of(asset.getStoragePath()), bytes);
          asset.setSizeBytes((long) bytes.length);
          asset.setUpdatedAt(LocalDateTime.now());
          files.updateById(asset);
        }
      } catch (Exception ignored) {
        return Map.of("error", 1);
      }
    }
    return Map.of("error", 0);
  }

  public List<PdfAnnotation> pdfAnnotations(Long userId, Long fileId) {
    requiredFile(userId, fileId);
    return pdfAnnotations.selectList(new LambdaQueryWrapper<PdfAnnotation>()
        .eq(PdfAnnotation::getUserId, userId)
        .eq(PdfAnnotation::getFileAssetId, fileId)
        .orderByAsc(PdfAnnotation::getPageNo));
  }

  public PdfAnnotation createPdfAnnotation(Long userId, Long fileId, Requests.PdfAnnotationCreate req) {
    requiredFile(userId, fileId);
    PdfAnnotation annotation = new PdfAnnotation();
    BeanUtils.copyProperties(req, annotation);
    annotation.setUserId(userId);
    annotation.setFileAssetId(fileId);
    annotation.setAnnotationType(defaultString(annotation.getAnnotationType(), "HIGHLIGHT"));
    pdfAnnotations.insert(annotation);
    return annotation;
  }

  public StudyNote excerptPdf(Long userId, Long fileId, Requests.PdfExcerpt req) {
    FileAsset asset = requiredFile(userId, fileId);
    StudyNote note = new StudyNote();
    note.setUserId(userId);
    note.setGoalId(req.goalId);
    note.setTaskId(req.taskId);
    note.setErrorLogId(req.errorLogId);
    note.setTitle(defaultString(req.title, "PDF 摘录：" + asset.getOriginalName()));
    note.setNoteType("MARKDOWN");
    note.setSourceFileId(fileId);
    note.setContentMarkdown("> 来源：" + asset.getOriginalName() + "\n\n" + defaultString(req.selectedText, ""));
    notes.insert(note);
    return note;
  }

  public Path exportNoteMarkdown(Long userId, Long noteId) throws IOException {
    StudyNote note = requiredNote(userId, noteId);
    Files.createDirectories(exportsDir);
    Path target = exportsDir.resolve("note-" + noteId + ".md");
    Files.writeString(target, defaultString(note.getContentMarkdown(), ""), StandardCharsets.UTF_8);
    return target;
  }

  public Path exportNotePdf(Long userId, Long noteId) throws IOException {
    StudyNote note = requiredNote(userId, noteId);
    Files.createDirectories(exportsDir);
    List<Extension> extensions = List.of(TablesExtension.create());
    Parser parser = Parser.builder().extensions(extensions).build();
    HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
    Node document = parser.parse(defaultString(note.getContentMarkdown(), ""));
    String html = "<!doctype html><html><head><meta charset='utf-8'><style>body{font-family:Arial,sans-serif;padding:36px;line-height:1.7}pre{background:#f4f4f4;padding:12px;border-radius:6px;white-space:pre-wrap}</style></head><body>"
        + renderer.render(document) + "</body></html>";
    Path target = exportsDir.resolve("note-" + noteId + ".pdf");
    com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
    try (var out = Files.newOutputStream(target)) {
      builder.withHtmlContent(html, null);
      builder.toStream(out);
      builder.run();
    }
    return target;
  }

  private Map<String, Object> loginToken(User user) {
    return Map.of("token", jwtUtil.create(user.getId(), user.getUsername()), "user", sanitizeUser(user));
  }

  private User requiredUser(Long userId) {
    User user = users.selectById(userId);
    if (user == null) throw new BizException(401, "用户不存在");
    return user;
  }

  private Map<String, Object> sanitizeUser(User user) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", user.getId());
    map.put("username", user.getUsername());
    map.put("email", user.getEmail());
    map.put("nickname", user.getNickname());
    map.put("avatarUrl", user.getAvatarUrl());
    map.put("theme", user.getTheme());
    map.put("createdAt", user.getCreatedAt());
    return map;
  }

  private Goal requiredGoal(Long userId, Long id) {
    Goal goal = goals.selectById(id);
    if (goal == null || !Objects.equals(goal.getUserId(), userId)) throw new BizException("目标不存在");
    return goal;
  }

  private GoalTemplate requiredGoalTemplate(Long id) {
    GoalTemplate template = goalTemplates.selectById(id);
    if (template == null) throw new BizException("目标模板不存在");
    return template;
  }

  private com.learntrace.entity.Task requiredTask(Long userId, Long id) {
    com.learntrace.entity.Task task = tasks.selectById(id);
    if (task == null || !Objects.equals(task.getUserId(), userId)) throw new BizException("任务不存在");
    return task;
  }

  private ErrorLog requiredError(Long userId, Long id) {
    ErrorLog error = errors.selectById(id);
    if (error == null || !Objects.equals(error.getUserId(), userId)) throw new BizException("错误记录不存在");
    return error;
  }

  private StudyNote requiredNote(Long userId, Long id) {
    StudyNote note = notes.selectById(id);
    if (note == null || !Objects.equals(note.getUserId(), userId)) throw new BizException("笔记不存在");
    return note;
  }

  private FileAsset requiredFile(Long userId, Long id) {
    FileAsset asset = files.selectById(id);
    if (asset == null || !Objects.equals(asset.getUserId(), userId)) throw new BizException("文件不存在");
    return asset;
  }

  private void upsertGoalChildren(Long userId, Long goalId, Requests.GoalUpsert req) {
    if (req.stages != null) {
      for (Requests.StageDraft draft : req.stages) {
        GoalStage stage = draft.id == null ? new GoalStage() : stages.selectById(draft.id);
        if (stage == null || (stage.getId() != null && !Objects.equals(stage.getUserId(), userId))) throw new BizException("阶段不存在");
        BeanUtils.copyProperties(draft, stage);
        stage.setUserId(userId);
        stage.setGoalId(goalId);
        stage.setStatus(defaultString(stage.getStatus(), "NOT_STARTED"));
        if (stage.getId() == null) stages.insert(stage); else stages.updateById(stage);
      }
    }
    if (req.tasks != null) {
      for (Requests.TaskDraft draft : req.tasks) {
        if (draft.id == null) {
          draft.goalId = goalId;
          createTask(userId, draft);
        } else {
          draft.goalId = goalId;
          updateTask(userId, draft.id, draft);
        }
      }
    }
  }

  private void recalcGoalProgress(Long userId, Long goalId) {
    long total = tasks.selectCount(new LambdaQueryWrapper<com.learntrace.entity.Task>().eq(com.learntrace.entity.Task::getUserId, userId).eq(com.learntrace.entity.Task::getGoalId, goalId));
    long done = tasks.selectCount(new LambdaQueryWrapper<com.learntrace.entity.Task>().eq(com.learntrace.entity.Task::getUserId, userId).eq(com.learntrace.entity.Task::getGoalId, goalId).eq(com.learntrace.entity.Task::getStatus, "DONE"));
    Goal goal = goals.selectById(goalId);
    if (goal != null) {
      goal.setProgress(total == 0 ? 0 : (int) Math.round(done * 100.0 / total));
      if (goal.getProgress() >= 100) goal.setStatus("COMPLETED");
      goal.setUpdatedAt(LocalDateTime.now());
      goals.updateById(goal);
    }
  }

  private void saveErrorTags(Long userId, Long errorId, Requests.ErrorUpsert req) {
    errorTags.delete(new LambdaQueryWrapper<ErrorLogTag>().eq(ErrorLogTag::getErrorLogId, errorId));
    Set<Long> tagIds = new LinkedHashSet<>();
    if (req.tagIds != null) tagIds.addAll(req.tagIds);
    if (req.tagNames != null) {
      for (String name : req.tagNames) tagIds.add(ensureTag(userId, name, null).getId());
    }
    for (Long tagId : tagIds) {
      Tag tag = tags.selectById(tagId);
      if (tag == null || !Objects.equals(tag.getUserId(), userId)) continue;
      ErrorLogTag link = new ErrorLogTag();
      link.setErrorLogId(errorId);
      link.setTagId(tagId);
      errorTags.insert(link);
    }
  }

  private Tag ensureTag(Long userId, String name, String color) {
    if (name == null || name.isBlank()) throw new BizException("标签名不能为空");
    Tag existing = tags.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getUserId, userId).eq(Tag::getName, name));
    if (existing != null) return existing;
    Tag tag = new Tag();
    tag.setUserId(userId);
    tag.setName(name.trim());
    tag.setColor(defaultString(color, "#f6c453"));
    tags.insert(tag);
    return tag;
  }

  private Map<String, Object> errorWithTags(ErrorLog error) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("error", error);
    List<Long> ids = errorTags.selectList(new LambdaQueryWrapper<ErrorLogTag>().eq(ErrorLogTag::getErrorLogId, error.getId()))
        .stream().map(ErrorLogTag::getTagId).toList();
    data.put("tags", ids.isEmpty() ? List.of() : tags.selectBatchIds(ids));
    return data;
  }

  private List<Map<String, Object>> aggregate(Long userId, String column) {
    QueryWrapper<ErrorLog> wrapper = new QueryWrapper<>();
    wrapper.select(column + " as name", "count(*) as value")
        .eq("user_id", userId)
        .groupBy(column)
        .orderByDesc("value");
    return errors.selectMaps(wrapper);
  }

  private static boolean has(Map<String, String> map, String key) {
    return map != null && map.get(key) != null && !map.get(key).isBlank();
  }

  private static Long parseLong(String value) {
    if (value == null || value.isBlank()) return null;
    return Long.valueOf(value);
  }

  private static String defaultString(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }

  private static String extension(String name) {
    int i = name.lastIndexOf('.');
    return i < 0 ? "" : name.substring(i + 1);
  }

  private static String stripExt(String name) {
    int i = name.lastIndexOf('.');
    return i < 0 ? name : name.substring(0, i);
  }
}
