package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("plan_templates")
public class PlanTemplate {
  private Long id;
  private String code;
  private String title;
  private String description;
  private String sourceType;
  private LocalDateTime createdAt;
}
