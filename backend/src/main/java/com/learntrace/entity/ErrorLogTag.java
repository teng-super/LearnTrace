package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("error_log_tags")
public class ErrorLogTag {
  private Long id;
  private Long errorLogId;
  private Long tagId;
}
