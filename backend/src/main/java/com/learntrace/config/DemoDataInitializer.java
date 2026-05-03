package com.learntrace.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learntrace.dto.Requests;
import com.learntrace.entity.User;
import com.learntrace.mapper.ErrorLogMapper;
import com.learntrace.mapper.StudyNoteMapper;
import com.learntrace.mapper.UserMapper;
import com.learntrace.service.CoreService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemoDataInitializer implements CommandLineRunner {
  private final UserMapper users;
  private final ErrorLogMapper errors;
  private final StudyNoteMapper notes;
  private final PasswordEncoder passwordEncoder;
  private final CoreService service;

  public DemoDataInitializer(UserMapper users, ErrorLogMapper errors, StudyNoteMapper notes,
                             PasswordEncoder passwordEncoder, CoreService service) {
    this.users = users;
    this.errors = errors;
    this.notes = notes;
    this.passwordEncoder = passwordEncoder;
    this.service = service;
  }

  @Override
  public void run(String... args) {
    User demo = users.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "demo"));
    if (demo == null) {
      demo = new User();
      demo.setUsername("demo");
      demo.setEmail("demo@learntrace.local");
      demo.setPasswordHash(passwordEncoder.encode("learntrace123"));
      demo.setNickname("音视频求职 Demo");
      demo.setTheme("battle-dark");
      users.insert(demo);
    }
    Long userId = demo.getId();
    try {
      service.importDefaultPlan(userId);
    } catch (Exception ignored) {
      // Demo data should never block application startup.
    }
    if (errors.selectCount(new LambdaQueryWrapper<com.learntrace.entity.ErrorLog>().eq(com.learntrace.entity.ErrorLog::getUserId, userId)) == 0) {
      Requests.ErrorUpsert e1 = new Requests.ErrorUpsert();
      e1.title = "epoll ET 模式忘记循环读导致连接卡住";
      e1.errorType = "NETWORK";
      e1.severity = "HIGH";
      e1.status = "NEED_REVIEW";
      e1.description = "压测时连接建立成功但回显偶发卡死，原因是 ET 模式只读了一次 recv。";
      e1.wrongCode = "if (events[i].events & EPOLLIN) { recv(fd, buf, sizeof(buf), 0); }";
      e1.correctCode = "while (true) {\\n  ssize_t n = recv(fd, buf, sizeof(buf), 0);\\n  if (n < 0 && (errno == EAGAIN || errno == EWOULDBLOCK)) break;\\n}";
      e1.reason = "边缘触发只在状态变化时通知，必须读到 EAGAIN。";
      e1.solution = "所有非阻塞 fd 都使用 while 循环读写，并统一处理 EAGAIN。";
      e1.summary = "ET 模式的核心不是收到事件，而是一次性耗尽可读状态。";
      e1.tagNames = List.of("epoll", "网络编程", "高频错误");
      service.createError(userId, e1);

      Requests.ErrorUpsert e2 = new Requests.ErrorUpsert();
      e2.title = "FFmpeg time_base 换算遗漏导致音视频时间戳错乱";
      e2.errorType = "FFMPEG";
      e2.severity = "MEDIUM";
      e2.status = "UNRESOLVED";
      e2.description = "remux mp4 到 flv 后播放进度跳变，PTS/DTS 未按输出流 time_base 重缩放。";
      e2.wrongCode = "outPacket.pts = inPacket.pts;";
      e2.correctCode = "outPacket.pts = av_rescale_q_rnd(inPacket.pts, inTimeBase, outTimeBase, AV_ROUND_NEAR_INF);";
      e2.reason = "不同封装和流的 time_base 不一致，直接复制时间戳会破坏时间轴。";
      e2.solution = "写包前统一使用 av_packet_rescale_ts 或 av_rescale_q。";
      e2.summary = "看到 PTS/DTS 就先问：它属于哪个 time_base。";
      e2.tagNames = List.of("FFmpeg", "time_base", "remux");
      service.createError(userId, e2);
    }
    if (notes.selectCount(new LambdaQueryWrapper<com.learntrace.entity.StudyNote>().eq(com.learntrace.entity.StudyNote::getUserId, userId)) == 0) {
      Requests.NoteUpsert note = new Requests.NoteUpsert();
      note.title = "音视频求职主线阶段复盘";
      note.noteType = "MARKDOWN";
      note.contentMarkdown = """
          # 音视频求职主线阶段复盘

          ## 当前策略
          - 首选客户端播放器 / SDK
          - 第二路线补流媒体服务端
          - RTC 作为扩展，不把算法岗作为首要目标

          ## 下周动作
          1. 完成 epoll echo server 压测
          2. 把 remux demo 写成可展示项目
          3. 每个错误记录至少写一个复习结论
          """;
      service.createNote(userId, note);
    }
  }
}
