package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("goal_template_stages")
public class GoalTemplateStage {
  private Long id;
  private Long templateId;
  private String title;
  private String description;
  private Integer stageOrder;
}
