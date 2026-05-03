package com.learntrace;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.learntrace.mapper")
public class LearnTraceApplication {
  public static void main(String[] args) {
    SpringApplication.run(LearnTraceApplication.class, args);
  }
}
