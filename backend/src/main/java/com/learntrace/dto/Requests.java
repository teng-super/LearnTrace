package com.learntrace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

public class Requests {
  @Data
  public static class Register {
    @NotBlank public String username;
    @Email public String email;
    @NotBlank public String password;
    public String nickname;
  }

  @Data
  public static class Login {
    @NotBlank public String usernameOrEmail;
    @NotBlank public String password;
  }

  @Data
  public static class ProfileUpdate {
    public String nickname;
    public String avatarUrl;
    public String theme;
  }

  @Data
  public static class PasswordUpdate {
    @NotBlank public String oldPassword;
    @NotBlank public String newPassword;
  }

  @Data
  public static class GoalUpsert {
    @NotBlank public String title;
    public String description;
    public String category;
    public String priority;
    public String status;
    public LocalDate startDate;
    public LocalDate deadline;
    public List<StageDraft> stages;
    public List<TaskDraft> tasks;
  }

  @Data
  public static class StageDraft {
    public Long id;
    @NotBlank public String title;
    public String description;
    public Integer stageOrder;
    public String status;
    public LocalDate startDate;
    public LocalDate deadline;
  }

  @Data
  public static class TaskDraft {
    public Long id;
    public Long goalId;
    public Long stageId;
    public Integer weekNo;
    @NotBlank public String title;
    public String description;
    public String taskType;
    public String status;
    public String priority;
    public LocalDate dueDate;
  }

  @Data
  public static class StatusPatch {
    @NotBlank public String status;
  }

  @Data
  public static class ImportTemplate {
    @NotNull public Long templateId;
    public LocalDate startDate;
    public String title;
    public String priority;
  }

  @Data
  public static class CustomFromPlan {
    public String title;
    public String description;
    public LocalDate startDate;
    public List<Long> planTaskIds;
  }

  @Data
  public static class ErrorUpsert {
    public Long goalId;
    public Long taskId;
    @NotBlank public String title;
    public String errorType;
    public String description;
    public String wrongCode;
    public String correctCode;
    public String reason;
    public String solution;
    public String summary;
    public String severity;
    public String status;
    public List<Long> tagIds;
    public List<String> tagNames;
  }

  @Data
  public static class ReviewSubmit {
    @NotBlank public String result;
    public String note;
  }

  @Data
  public static class TagCreate {
    @NotBlank public String name;
    public String color;
  }

  @Data
  public static class NoteUpsert {
    public Long goalId;
    public Long taskId;
    public Long errorLogId;
    @NotBlank public String title;
    public String contentMarkdown;
    public String noteType;
    public Long sourceFileId;
  }

  @Data
  public static class PdfAnnotationCreate {
    @NotNull public Integer pageNo;
    public String annotationType;
    public String selectedText;
    public String note;
    public String rectJson;
  }

  @Data
  public static class PdfExcerpt {
    public String title;
    public String selectedText;
    public Long goalId;
    public Long taskId;
    public Long errorLogId;
  }
}
