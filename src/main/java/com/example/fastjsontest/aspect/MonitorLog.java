package com.example.fastjsontest.aspect;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MonitorLog {

    /**
     * requestId
     */
    String requestIdName() default Constants.headerRequestId;

    /**
     * channel
     */
    String channelName() default Constants.headerAppKey;

    /**
     * 请求头字段
     */
    String[] headerNames() default {Constants.headerAppKey, Constants.headerTimestamp, Constants.headerVersion, Constants.headerSign, Constants.headerRequestId};

    /**
     * 主要监控字段名称：billNo | orderNo
     */
    String monitorFieldName() default "";

    /**
     * 参数类型
     */
    Class parameterClassType() default Object.class;

    /**
     * 返回结果类型
     */
    Class resultClassType() default Objects.class;

    /**
     * 每一条记录状态监控字段
     */
    String monitorCodeFieldName() default "code";

    /**
     * 每一条记录消息监控字段
     */
    String monitorMsgFieldName() default "msg";

}
