package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tags")
public class Tag {
  private Long id;
  private Long userId;
  private String name;
  private String color;
  private LocalDateTime createdAt;
}
