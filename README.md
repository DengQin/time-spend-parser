# time-spend-parser：一个接口、方法耗时分析工具

1、引入系统<br>

spring文件引入配置

<import resource="classpath*:applicationContext-timespend.xml" />

=== 1.1 web系统 ===
springmvc-servlet.xml引入配置

        <!-- 系统耗时分析,设置显示request的参数 -->
        <bean id="timeParams" class="java.util.LinkedList">
                <constructor-arg>
                        <list>
                                <value>callback</value>
                        </list>
                </constructor-arg>
        </bean>

        <!-- 系统耗时分析,设置要查询的uri和显示request的参数 -->
        <bean id="timeParserWebInterceptor" class="com.duowan.dengqin.util.timespend.ParserWebInterceptor">
                <property name="uriParamsMap">
                        <map>
                                <entry key="/client/get.do" value-ref="timeParams" />
                        </map>
                </property>
        </bean>


        <!-- HandlerMapping生成器 -->
        <bean
                class="org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping">
                <!-- 拦截器注册 -->
                <property name="interceptors">
                        <list>
                                <ref bean="timeParserWebInterceptor" />
                                <bean class="xxxxInterceptor" />  <!-- 原来的配置 -->
                        </list>
                </property>
        </bean>

=== 1.2 thrift接口 ===

                TimeParser.init("方法名");
                TimeParser.start();
                方法的执行
                TimeParser.end();

需要计算某个方法耗时，要修改代码，加入

                TimeParser.addStartTime("querySomeCode");
                QuerySomeCodeResult querySomeCodeResult = Utils.querySomeCode(request, response);
                TimeParser.addEndTime("querySomeCode");

2、分析文件<br>

获取数据

grep 'TimeConsumer \[uri=' /data2/log/resin/stdout.log >aa.txt

分析数据

java -cp "D:\maven-repository\com\dengqin\util\time-spend-parser\1.0\time-spend-parser-1.0-SNAPSHOT.jar" "com.dengqin.util.timespend.Parser" "d:/aa.txt" >d:result.txt

文件输出示例

[2013-03-04 18:12:20,263] [] [INFO ] <com.dengqin.util.timespend.TimeParser> - TimeConsumer [uri=/client/getCode.do, allUseTime=14,params={callback=234}, startTime=1362391940249, timeSpend=[CodeGroupService_getCodeGroup_start:1, CodeGroupDao_getCodeGroup_start:2, CodeGroupDao_getCodeGroup_end:5, CodeGroupService_getCodeGroup_end:7, CodeService_getCodeByUid_start:9, CodeDao_getCodeByUid_start:10, CodeDao_getCodeByUid_end:12, CodeService_getCodeByUid_end:14]](TimeParser.java:end:64)
