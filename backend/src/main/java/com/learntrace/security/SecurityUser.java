package com.learntrace.security;

import com.learntrace.common.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUser {
  private SecurityUser() {}

  public static Long id() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof AuthUser user)) {
      throw new BizException(401, "请先登录");
    }
    return user.id();
  }
}
