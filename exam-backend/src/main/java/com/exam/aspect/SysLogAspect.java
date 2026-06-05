package com.exam.aspect;

import cn.hutool.json.JSONUtil;
import com.exam.annotation.OperateLog;
import com.exam.entity.SysLog;
import com.exam.mapper.SysLogMapper;
import com.exam.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SysLogAspect {

    private final SysLogMapper sysLogMapper;

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint point, OperateLog operateLog) throws Throwable {
        long start = System.currentTimeMillis();
        com.exam.entity.SysLog sysLog = new com.exam.entity.SysLog();
        sysLog.setOperation(operateLog.value());

        try {
            // 获取当前用户
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                sysLog.setUserId(principal.getUserId());
                sysLog.setUsername(principal.getUsername());
            }

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                sysLog.setIp(request.getRemoteAddr());
                sysLog.setMethod(request.getMethod() + " " + request.getRequestURI());
            }

            // 记录参数（截断防止过长）
            Object[] args = point.getArgs();
            String params = JSONUtil.toJsonStr(args);
            if (params.length() > 1000) params = params.substring(0, 1000) + "...";
            sysLog.setParams(params);

            Object result = point.proceed();

            sysLog.setStatus(1);
            sysLog.setExecuteTime(System.currentTimeMillis() - start);
            sysLogMapper.insert(sysLog);
            return result;
        } catch (Throwable e) {
            sysLog.setStatus(0);
            sysLog.setErrorMsg(e.getMessage());
            sysLog.setExecuteTime(System.currentTimeMillis() - start);
            sysLogMapper.insert(sysLog);
            throw e;
        }
    }
}
