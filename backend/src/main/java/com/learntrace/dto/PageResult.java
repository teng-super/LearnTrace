package com.learntrace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {
  private long total;
  private long page;
  private long size;
  private List<T> records;
}
