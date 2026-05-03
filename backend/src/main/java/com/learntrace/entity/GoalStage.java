package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("goal_stages")
public class GoalStage {
  private Long id;
  private Long userId;
  private Long goalId;
  private String title;
  private String description;
  private Integer stageOrder;
  private String status;
  private LocalDate startDate;
  private LocalDate deadline;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
