package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("goals")
public class Goal {
  private Long id;
  private Long userId;
  private String title;
  private String description;
  private String category;
  private String priority;
  private String status;
  private LocalDate startDate;
  private LocalDate deadline;
  private Integer progress;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
