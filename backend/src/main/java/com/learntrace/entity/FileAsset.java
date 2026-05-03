package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_assets")
public class FileAsset {
  private Long id;
  private Long userId;
  private String originalName;
  private String storedName;
  private String storagePath;
  private String fileType;
  private String mimeType;
  private Long sizeBytes;
  private String relationType;
  private Long relationId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
