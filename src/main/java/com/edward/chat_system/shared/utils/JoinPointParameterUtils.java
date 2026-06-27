package com.edward.chat_system.shared.utils;

import com.edward.chat_system.infrastructure.aop.annotation.ServerId;
import java.lang.reflect.Parameter;
import lombok.experimental.UtilityClass;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

@UtilityClass
public class JoinPointParameterUtils {
    public String getServerIdParam(JoinPoint jp) {
        Parameter[] params = ((MethodSignature) jp.getSignature()).getMethod().getParameters();
        Object[] args = jp.getArgs();

        String serverId = null;

        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(ServerId.class)) {
                serverId = (String) args[i];
                break;
            }
        }
        return serverId;
    }
}
