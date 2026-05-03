package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review_records")
public class ReviewRecord {
  private Long id;
  private Long userId;
  private Long errorLogId;
  private String result;
  private String note;
  private LocalDateTime reviewedAt;
  private LocalDateTime nextReviewAt;
}
