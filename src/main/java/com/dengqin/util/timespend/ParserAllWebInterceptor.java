package com.dengqin.util.timespend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局拦截器,拦截全部的请求用的
 */
public class ParserAllWebInterceptor extends HandlerInterceptorAdapter implements InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(ParserAllWebInterceptor.class);

	// 全局拦截功能
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String requestURI = request.getRequestURI();
		TimeParser.init(requestURI);
		TimeParser.start();
		return true;
	}

	// 会在每个请求的最后执行
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		TimeParser.end();
	}

	// 在系统启动时会执行
	public void afterPropertiesSet() throws Exception {
		log.info("启动耗时分析,监控全部请求！");
	}

}
