package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("study_notes")
public class StudyNote {
  private Long id;
  private Long userId;
  private Long goalId;
  private Long taskId;
  private Long errorLogId;
  private String title;
  private String contentMarkdown;
  private String noteType;
  private Long sourceFileId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
