package com.dengqin.util.timespend;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * 方法拦截器
 */
public class ParserMethodInterceptor implements MethodInterceptor {

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Method method = invocation.getMethod();
		TimeParser.startMethod(method);
		Object result = invocation.proceed();
		TimeParser.endMethod(method);
		return result;
	}
}
