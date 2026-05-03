package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("plan_template_tasks")
public class PlanTemplateTask {
  private Long id;
  private Long templateId;
  private Integer weekNo;
  private String sectionTitle;
  private String title;
  private String description;
  private String taskType;
  private Integer sortOrder;
}
