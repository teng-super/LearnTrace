package com.learntrace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pdf_annotations")
public class PdfAnnotation {
  private Long id;
  private Long userId;
  private Long fileAssetId;
  private Integer pageNo;
  private String annotationType;
  private String selectedText;
  private String note;
  private String rectJson;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
