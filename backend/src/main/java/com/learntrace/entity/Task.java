package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tasks")
public class Task {
  private Long id;
  private Long userId;
  private Long goalId;
  private Long stageId;
  private Integer weekNo;
  private String title;
  private String description;
  private String taskType;
  private String status;
  private String priority;
  private LocalDate dueDate;
  private LocalDateTime completedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
