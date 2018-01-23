package com.dengqin.util.timespend;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * 解析器
 */
public class Parser {
	static List<String> uriList = new LinkedList<String>();
	static List<String> paramsList = new LinkedList<String>();

	public static void main(String[] args) {
		if (args == null || args.length != 1) {
			System.out.println("参数个数不为1，请指定文件路径");
			return;
		}
		// 读取指定的日志文件
		uriList = readFile(args[0]);
		// 计算
		cal();
		System.out.println();
		System.out.println();
		// 汇总
		summary();
	}

	private static void cal() {
		Map<String, UriRequest> uriList = getUriListAndCalAllUseTime();
		for (Map.Entry<String, UriRequest> entry : uriList.entrySet()) {
			UriRequest uriRequest = entry.getValue();
			List<String> paramsList = uriRequest.getTimesList();
			if (paramsList.isEmpty()) {
				continue;
			}
			Map<String, Stat> statMap = new HashMap<String, Stat>();
			Map<String, List<Long>> timesMap = new HashMap<String, List<Long>>();
			for (String string : paramsList) {
				calMethodTime(string, statMap, timesMap);
			}
			LinkedList<String> memthodNameList = getMemthodNameList(paramsList.get(0));

			int times = paramsList.size();
			long deviationAllUserTime = 0;

			Stat allUseStat = uriRequest.getStat();
			List<Long> allUseTimeList = uriRequest.getUseTime();

			long avgAllUseTime = allUseStat.getSum() / times;
			for (Long t : allUseTimeList) {
				deviationAllUserTime += Math.abs(t - avgAllUseTime);
			}

			System.out.println("请求路径" + entry.getKey() + ",次数" + times + ",耗时:平均" + avgAllUseTime + ",最小"
					+ allUseStat.getMin() + ",最大" + allUseStat.getMax() + ",平均偏差" + deviationAllUserTime / times);
			for (String methodName : memthodNameList) {
				if (methodName == null) {
					System.out.println();
				}
				String key = methodName.trim();
				List<Long> timesList = timesMap.get(key);
				MethodTime methodTime = new MethodTime(methodName, timesList);
				System.out.println(methodTime.toConsole());
			}
			System.out.println();
		}
	}

	private static void summary() {
		Map<String, Stat> statMap = new HashMap<String, Stat>();
		Map<String, List<Long>> timesMap = new HashMap<String, List<Long>>();
		for (String string : paramsList) {
			calMethodTime(string, statMap, timesMap);
		}
		int times = paramsList.size();
		System.out.println("概述:");
		System.out.println("总访问次数" + times);
		for (Map.Entry<String, List<Long>> entry : timesMap.entrySet()) {
			String methodName = entry.getKey();
			List<Long> timesList = entry.getValue();
			MethodTime methodTime = new MethodTime(methodName, timesList);
			System.out.println(methodTime.toConsole());
		}
		System.out.println();
	}

	private static List<String> readFile(String fileName) {
		List<String> list = new LinkedList<String>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String s = "";
		try {
			while ((s = in.readLine()) != null) {
				if (s.contains("TimeConsumer [uri=")) {
					list.add(s);
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	// 获取方法的uri对应list
	private static Map<String, UriRequest> getUriListAndCalAllUseTime() {
		Map<String, UriRequest> map = new HashMap<String, UriRequest>();
		for (String string : uriList) {
			String uri = StringUtil.getBetweenString(string, "[uri=", ", ");
			String times = StringUtil.getBetweenString(string, "timeSpend=[", "]");
			if (StringUtil.isBlank(uri) || StringUtil.isBlank(times)) {
				continue;
			}
			paramsList.add(times);
			String[] arr = times.split(",");
			int length = arr.length;
			String str = "";
			for (String s : arr) {
				String[] arr2 = s.split(":");
				if (arr2.length == 2) {
					str += StringUtil.trim(arr2[0]);
				}
			}
			int flag = Math.abs(str.hashCode() % 100);
			String allUseTimeStr = StringUtil.getBetweenString(string, "allUseTime=", ",");
			long allUseTime = StringUtil.parseLong(allUseTimeStr);
			String key = uri + "_" + length + flag;
			UriRequest uriRequest = map.get(key);
			if (uriRequest == null) {
				uriRequest = new UriRequest(key);
				uriRequest.addTimes(times);
				uriRequest.addUseTime(allUseTime);
				map.put(key, uriRequest);
			} else {
				uriRequest.addTimes(times);
				uriRequest.getStat().addTime(allUseTime);
			}
		}
		return map;
	}

	// 求出方法名的list
	private static LinkedList<String> getMemthodNameList(String string) {
		Stack<String> stack = new Stack<String>();
		Map<String, String> map = new HashMap<String, String>();
		LinkedList<String> list = new LinkedList<String>();
		for (String str : string.split(",")) {
			String[] arr = str.split(":");
			if (arr.length != 2) {
				continue;
			}
			String key = StringUtil.trim(arr[0]);
			if (key.endsWith("_start")) {
				String name = key.substring(0, key.length() - 6);
				stack.push(name);
				list.add(name);
				continue;
			}
			if (key.endsWith("_end")) {
				key = key.substring(0, key.length() - 4);
				String start = stack.pop();
				if (start.equals(key)) {
					String name = key;
					for (int i = stack.size(); i > -1; i--) {
						name = "\t" + name;
					}
					map.put(key, name);
				} else {
					throw new RuntimeException(string + "的" + key + "方法找不到开始或结束时间");
				}
			}

		}

		LinkedList<String> result = new LinkedList<String>();
		for (String str : list) {
			String methodName = map.get(str);
			if (methodName == null) {
				throw new RuntimeException(string + "的" + str + "方法找不到开始或结束时间");
			}
			result.add(methodName);
		}
		return result;

	}

	// 计算方法名对应的时间
	public static void calMethodTime(String string, Map<String, Stat> statMap, Map<String, List<Long>> timesMap) {
		Stack<StackVo> stack = new Stack<StackVo>();
		for (String str : string.split(",")) {
			String[] arr = str.split(":");
			if (arr.length != 2) {
				continue;
			}
			String key = StringUtil.trim(arr[0]);
			Long time = StringUtil.parseLong(StringUtil.trim(arr[1]));
			if (key.endsWith("_start")) {
				StackVo start = new StackVo(key.substring(0, key.length() - 6), time);
				stack.push(start);
				continue;
			}
			if (key.endsWith("_end")) {
				key = key.substring(0, key.length() - 4);
				StackVo start = stack.pop();
				if (start.getName().equals(key)) {
					long consumeTime = time - start.getTime();
					if (consumeTime < 0) {
						throw new RuntimeException(key + "的结束时间早于开始时间,该字符串为" + string);
					}
					// 设置消耗时间
					List<Long> timeList = timesMap.get(key);
					if (timeList == null) {
						timeList = new LinkedList<Long>();
						timeList.add(consumeTime);
						timesMap.put(key, timeList);
					} else {
						timeList.add(consumeTime);
					}
					// 设置统计
					Stat stat = statMap.get(key);
					if (stat == null) {
						stat = new Stat();
						stat.addTime(consumeTime);
						statMap.put(key, stat);
					} else {
						stat.addTime(consumeTime);
					}
				} else {
					throw new RuntimeException(string + "的" + key + "方法找不到开始或结束时间");
				}
			}

		}
	}

}

class UriRequest {
	String uri;
	List<String> timesList = new LinkedList<String>();
	List<Long> useTime = new LinkedList<Long>();
	Stat stat = new Stat();

	public UriRequest() {
		super();
	}

	public UriRequest(String uri) {
		super();
		this.uri = uri;
	}

	public void addTimes(String times) {
		timesList.add(times);
	}

	public void addUseTime(Long userTime) {
		stat.addTime(userTime);
		useTime.add(userTime);
	}

	public String getUri() {
		return uri;
	}

	public List<String> getTimesList() {
		return timesList;
	}

	public List<Long> getUseTime() {
		return useTime;
	}

	public Stat getStat() {
		return stat;
	}

}

class StackVo {
	String name;
	long time;

	public StackVo(String name, long time) {
		super();
		this.name = name;
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

}

class MethodTime {
	String name;
	long avgTime = 0;
	Stat stat;
	// 偏差
	long deviation = 0;

	public MethodTime(String name, List<Long> timesList) {
		super();
		this.name = name;
		int size = timesList.size();
		if (size == 0) {
			stat = new Stat(0, 0, 0);
			return;
		}
		stat = new Stat();
		for (long t : timesList) {
			stat.addTime(t);
		}
		long avgTime = stat.getSum() / size;
		this.avgTime = avgTime;
		long deviationAll = 0;
		for (long t : timesList) {
			deviationAll += Math.abs(t - avgTime);
		}
		this.deviation = (deviationAll / size);
	}

	public String getName() {
		return name;
	}

	public String toConsole() {
		return name + "耗时:平均" + avgTime + ",最小" + stat.getMin() + ",最大" + stat.getMax() + ",平均偏差" + deviation;
	}
}

class Stat {
	long sum = 0;
	long min = Long.MAX_VALUE;
	long max = 0;

	public Stat() {
		super();
	}

	public Stat(long sum, long min, long max) {
		super();
		this.sum = sum;
		this.min = min;
		this.max = max;
	}

	public long getSum() {
		return sum;
	}

	public void addTime(long time) {
		this.sum += time;
		if (time < this.min) {
			this.min = time;
		}
		if (time > this.max) {
			this.max = time;
		}
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

}

class StringUtil {
	/**
	 * 把字符串转换为long
	 * 
	 * @param s
	 * @return
	 */
	public static Long parseLong(String s) {
		if (s == null || s == "") {
			return 0l;
		}
		long value = 0;
		for (int i = 0, len = s.length(); i < len; i++) {
			char c = s.charAt(i);
			int j = c - '0';
			if (j < 0 || j > 9) {
				return 0l;
			} else {
				value = 10 * value + j;
			}
		}
		return value;
	}

	public static String trim(String str) {
		if (str == null) {
			return "";
		}
		return str.trim();
	}

	/**
	 * 从s1中截取s2和s3中间的字符串,找不到返回""
	 * 
	 * @param s
	 *            源字符串
	 * @param c1
	 *            开始字符
	 * @param c2
	 *            结束字符
	 * @return
	 */
	public static String getBetweenString(String s, char c1, char c2) {
		char[] arr = s.toCharArray();
		boolean isFoundHead = false;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (!isFoundHead && arr[i] == c1) {// 找到开头
				isFoundHead = true;
			} else if (isFoundHead && arr[i] == c2) {// 找到结尾
				return sb.toString();
			} else if (isFoundHead) {
				sb.append(arr[i]);
			}
		}
		return "";
	}

	/**
	 * 从s1中截取s2和s3中间的字符串,找不到返回""
	 * 
	 * @param s1
	 *            源字符串
	 * @param s2
	 *            开始字符串
	 * @param s3
	 *            结束字符串
	 * @return
	 */
	public static String getBetweenString(String s1, String s2, String s3) {
		char[] arr1 = s1.toCharArray();
		char[] arr2 = s2.toCharArray();
		char[] arr3 = s3.toCharArray();
		boolean isFoundHead = false;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr1.length; i++) {
			if (arr1[i] == arr2[0] && isMatch(arr1, i, arr2) && !isFoundHead) {// 找到开头
				i += arr2.length;
				isFoundHead = true;
			}
			if (isFoundHead) {
				if (arr1[i] == arr3[0] && isMatch(arr1, i, arr3)) {// 找到结尾
					i += arr3.length - 1;
					return sb.toString().trim();
				} else {
					sb.append(arr1[i]);
				}
			}
		}
		return "";
	}

	// 从arr1的start匹配arr2
	public static boolean isMatch(char[] arr1, int start, char[] arr2) {
		for (int j = 0; j < arr2.length && start + j < arr1.length; j++) {
			if (arr1[start + j] != arr2[j]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 检查空字符串，或者" "字符串
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isBlank(String str) {
		if (str == null)
			return true;
		char[] arr = str.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			if (!Character.isWhitespace(arr[i])) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNotBlank(String str) {
		if (str == null)
			return false;
		char[] arr = str.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			if (!Character.isWhitespace(arr[i])) {
				return true;
			}
		}
		return false;
	}
}