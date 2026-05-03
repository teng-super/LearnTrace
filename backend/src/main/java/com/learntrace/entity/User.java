package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
  private Long id;
  private String username;
  private String email;
  private String passwordHash;
  private String nickname;
  private String avatarUrl;
  private String theme;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
