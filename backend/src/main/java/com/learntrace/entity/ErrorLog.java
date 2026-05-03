package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("error_logs")
public class ErrorLog {
  private Long id;
  private Long userId;
  private Long goalId;
  private Long taskId;
  private String title;
  private String errorType;
  private String description;
  private String wrongCode;
  private String correctCode;
  private String reason;
  private String solution;
  private String summary;
  private String severity;
  private String status;
  private Integer reviewCount;
  private LocalDateTime nextReviewAt;
  private LocalDateTime lastReviewedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
