package com.dengqin.util.timespend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 时间分析
 */
public class TimeParser {
	private static final Logger log = LoggerFactory.getLogger(TimeParser.class);

	private static ThreadLocal<TimeSpend> tl = new ThreadLocal<TimeSpend>();

	public static void init(String uri) {
		TimeSpend timeSpend = new TimeSpend();
		timeSpend.setUri(uri);
		tl.set(timeSpend);
	}

	public static void addParam(String key, String value) {
		TimeSpend timeSpend = tl.get();
		if (timeSpend != null) {
			timeSpend.addParam(key, value);
		}
	}

	public static void start() {
		TimeSpend timeSpend = tl.get();
		if (timeSpend != null) {
			timeSpend.setStartTime();
		}
	}

	static void startMethod(Method method) {
		TimeSpend timeSpend = tl.get();
		if (timeSpend != null) {
			addTimeConsumer(method, timeSpend, "_start");
		}
	}

	/**
	 * 手工添加监控代码,必须和addEndTime配对出现
	 */
	public static void addStartTime(String methodName) {
		TimeSpend timeSpend = tl.get();
		if (timeSpend != null) {
			timeSpend.addTimeSpend(methodName + "_start");
		}
	}

	/**
	 * 手工添加监控代码,必须和addStartTime配对出现
	 */
	public static void addEndTime(String methodName) {
		TimeSpend timeSpend = tl.get();
		if (timeSpend != null) {
			timeSpend.addTimeSpend(methodName + "_end");
		}
	}

	private static void addTimeConsumer(Method method, TimeSpend timeSpend, String msg) {
		String className = method.getDeclaringClass().getName();
		String name = className + "_" + method.getName();
		String desc = name.substring(name.lastIndexOf(".") + 1);
		timeSpend.addTimeSpend(desc + msg);
	}

	public static void endMethod(Method method) {
		TimeSpend timeSpend = tl.get();
		if (timeSpend != null) {
			addTimeConsumer(method, timeSpend, "_end");
		}
	}

	public static void end() {
		TimeSpend timeSpend = tl.get();
		if (timeSpend != null) {
			timeSpend.setAllUseTime();
			log.info(timeSpend.toString()); // 结束时候输出日志，用于统计分析计算
			tl.remove();
		}
	}
}
