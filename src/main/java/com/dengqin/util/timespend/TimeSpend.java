package com.dengqin.util.timespend;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 时间开销花费
 */
public class TimeSpend {

	private String uri;
	private Map<String, String> params = new HashMap<String, String>();
	private List<String> timeSpend = new LinkedList<String>();
	private long startTime;
	private long allUseTime;

	public void addParam(String key, String value) {
		params.put(key, value);
	}

	public void addTimeSpend(String key) {
		timeSpend.add(key + ":" + (new Date().getTime() - startTime));
	}

	public void setAllUseTime() {
		allUseTime = new Date().getTime() - startTime;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime() {
		this.startTime = new Date().getTime();
	}

	@Override
	public String toString() {
		return "TimeConsumer [uri=" + uri + ", allUseTime=" + allUseTime + ",params=" + params + ", startTime="
				+ startTime + ", timeSpend=" + timeSpend + "]";
	}

}
