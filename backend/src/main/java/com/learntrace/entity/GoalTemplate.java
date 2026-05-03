package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("goal_templates")
public class GoalTemplate {
  private Long id;
  private String code;
  private String title;
  private String description;
  private String category;
  private String priority;
  private String sourceType;
  private Integer recommendedOrder;
  private LocalDateTime createdAt;
}
