package com.example.fastjsontest.vo;

import lombok.Data;

import java.beans.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Data
public class MonitorLogDo implements Serializable {

    public MonitorLogDo() {
        reqTime = new Date();
    }

    /**
     * 请求ID
     */
    private String requestId;
    /**
     * 渠道
     */
    private String channel;
    /**
     * url
     */
    private String url;

    /**
     * 接口方法
     */
    private String method;

    /**
     * 监控单号
     */
    private String monitorNo;

    /**
     * 客户端Ip
     */
    private String sourceIp;

    /**
     * 请求头信息
     */
    private String reqHeader;

    /**
     * 请求时间
     */
    private Date reqTime;

    /**
     * 参数
     */
    private String reqParam;

    /**
     * 参数数据
     */
    private String reqData;

    /**
     * 返回码
     */
    private String resCode;

    /**
     * 返回消息
     */
    private String resMsg;

    /**
     * 返回结果
     */
    private String resResult;

    /**
     * 结果数据
     */
    private String resData;

    /**
     * 耗时(单位：毫秒)
     */
    private Long costTime;

    @Transient
    public void countCostTime() {
        if (Objects.isNull(reqTime)) {
            return;
        }
        costTime = System.currentTimeMillis() - reqTime.getTime();
    }
}
