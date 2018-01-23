package com.dengqin.util.timespend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 全局拦截器
 */
public class ParserWebInterceptor extends HandlerInterceptorAdapter implements InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(ParserWebInterceptor.class);

	public Map<String, List<String>> uriParamsMap;

	// 全局拦截功能
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String requestURI = request.getRequestURI();
		List<String> params = uriParamsMap.get(requestURI);
		if (params != null) {
			TimeParser.init(requestURI);
			if (!params.isEmpty()) {
				for (String string : params) {
					TimeParser.addParam(string, request.getParameter(string));// 添加参数列表
				}
			}
			TimeParser.start();
		}
		return true;
	}

	// 会在每个请求的最后执行
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		List<String> uriParams = uriParamsMap.get(request.getRequestURI());
		if (uriParams != null) {
			TimeParser.end();
		}
	}

	// 在系统启动时会执行
	public void afterPropertiesSet() throws Exception {
		log.info("启动耗时分析");
	}

	public void setUriParamsMap(Map<String, List<String>> uriParamsMap) {
		this.uriParamsMap = uriParamsMap;
	}

}
