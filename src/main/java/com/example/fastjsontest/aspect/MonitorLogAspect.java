package com.example.fastjsontest.aspect;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.fastjsontest.vo.GatewayRequestVo;
import com.example.fastjsontest.vo.GatewayResponseVo;
import com.example.fastjsontest.vo.MonitorLogDo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;


@Aspect
@Component
@Slf4j
public class MonitorLogAspect {

    @Pointcut("@annotation(com.example.fastjsontest.aspect.MonitorLog)")
    public void controllerAspect() {
    }


    @Around("controllerAspect()")
    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {
        MonitorContext.setOpenMonitorLog(true);
        MonitorLogDo monitorLogDo = new MonitorLogDo();
        MonitorLog monitorLog = beforeProceed(joinPoint, monitorLogDo);
        Object result = joinPoint.proceed();
        afterProceed(monitorLog, result, monitorLogDo);
        MonitorContext.removeOpenMonitorLog();
        return result;
    }

    private MonitorLog beforeProceed(ProceedingJoinPoint joinPoint, MonitorLogDo monitorLogDo) {
        MonitorLog monitorLog = null;
        monitorLogDo.setReqTime(new Date());
        try {
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            Method targetMethod = methodSignature.getMethod();
            Annotation[] annotations = targetMethod.getAnnotations();
//            monitorLogDo.setSourceIp(IpUtil.getRequestIp(request));
            monitorLogDo.setUrl(request.getRequestURI());
            String declaringClassName = targetMethod.getDeclaringClass().getName();
            monitorLogDo.setMethod(declaringClassName.substring(declaringClassName.lastIndexOf(".") + 1) + "." + targetMethod.getName()
            );
            monitorLog = (MonitorLog) Arrays.stream(annotations).filter(annotation -> annotation instanceof MonitorLog).findFirst().orElse(null);
            //获取参数
            monitorLogDo.setRequestId(this.getRequestId(request, monitorLog));
            monitorLogDo.setChannel(this.getChannel(request, monitorLog));
            monitorLogDo.setReqHeader(this.getReqHeader(request, monitorLog));
            monitorLogDo.setReqParam(IoUtil.read(request.getReader()));
            monitorLogDo.setMonitorNo(this.getMonitorNo(monitorLog, monitorLogDo.getReqParam(), request));
        } catch (Exception e) {
            log.error("监控入参处理异常:", e);
        }
        return monitorLog;
    }

    private String getChannel(HttpServletRequest request, MonitorLog monitorLog) {
        if (Objects.isNull(monitorLog) || ObjectUtil.isEmpty(monitorLog.headerNames())) {
            return null;
        }
        return request.getHeader(monitorLog.channelName());
    }

    private String getReqHeader(HttpServletRequest request, MonitorLog monitorLog) {
        if (Objects.isNull(monitorLog) || ObjectUtil.isEmpty(monitorLog.headerNames())) {
            return null;
        }
        JSONObject headers = new JSONObject();
        Arrays.stream(monitorLog.headerNames())
                .forEach(headerName -> headers.put(headerName, request.getHeader(headerName)));
        return headers.toJSONString();
    }

    private String getRequestId(HttpServletRequest request, MonitorLog monitorLog) {
        if (Objects.isNull(monitorLog) || ObjectUtil.isEmpty(monitorLog.requestIdName())) {
            return null;
        }
        return request.getHeader(monitorLog.requestIdName());
    }


    /**
     * 入参处理
     *
     * @param monitorLog 日志兼容注解
     * @param reqParam   入参
     * @param request    request
     */
    private String getMonitorNo(MonitorLog monitorLog, String reqParam, HttpServletRequest request) {
        if (Objects.isNull(monitorLog) || ObjectUtil.isEmpty(monitorLog.monitorFieldName())) {
            return null;
        }
        String monitorNo = getMonitorNoFromReqParam(monitorLog, reqParam);
        if (ObjectUtil.isNotNull(monitorNo)) {
            return monitorNo;
        }
        return request.getHeader(monitorLog.monitorFieldName());
    }

    private String getMonitorNoFromReqParam(MonitorLog monitorLog, String reqParam) {
        if (ObjectUtil.isEmpty(reqParam)) {
            return null;
        }
        if (GatewayRequestVo.class.equals(monitorLog.parameterClassType())) {
            GatewayRequestVo gatewayRequestVo = JSON.parseObject(reqParam, GatewayRequestVo.class);
            if (Objects.isNull(gatewayRequestVo)) {
                return null;
            }
            JSONObject dataJson = JSON.parseObject(JSON.toJSONString(gatewayRequestVo.getData()));
            if (ObjectUtil.isNotNull(dataJson) && ObjectUtil.isNotNull(dataJson.get(monitorLog.monitorFieldName()))) {
                return String.valueOf(dataJson.get(monitorLog.monitorFieldName()));
            }
        } else {
            JSONObject dataJson = JSON.parseObject(reqParam);
            if (ObjectUtil.isNotNull(dataJson) && ObjectUtil.isNotNull(dataJson.get(monitorLog.monitorFieldName()))) {
                return String.valueOf(dataJson.get(monitorLog.monitorFieldName()));
            }
        }
        return null;
    }

    /**
     * afterProceed
     *
     * @param monitorLog   monitorLog
     * @param result       result
     * @param monitorLogDo monitorLogDo
     */
    private void afterProceed(MonitorLog monitorLog, Object result, MonitorLogDo monitorLogDo) {
        try {
            this.reqParamHandleAgain(monitorLog, monitorLogDo);
            this.resParamHandle(monitorLog, result, monitorLogDo);
            monitorLogDo.countCostTime();
//            mqRepository.monitorLogAdd(monitorLogDo);
        } catch (Exception e) {
            log.error("监控结果处理异常：", e);
        }
    }

    private void reqParamHandleAgain(MonitorLog monitorLog, MonitorLogDo monitorLogDo) {
        if (Objects.isNull(monitorLog) || Objects.isNull(monitorLogDo)) {
            return;
        }
        monitorLogDo.setReqData(MonitorContext.removeUnencryptedReqData());
        if (ObjectUtil.isEmpty(monitorLogDo.getMonitorNo()) && ObjectUtil.isNotEmpty(monitorLog.monitorFieldName())) {
            JSONObject dataJson = JSON.parseObject(monitorLogDo.getReqData());
            if (Objects.isNull(dataJson)) {
                return;
            }
            monitorLogDo.setMonitorNo(dataJson.getString(monitorLog.monitorFieldName()));
        }
    }

    /**
     * 返回结果处理
     *
     * @param result     返回结果
     * @param monitorLog 监控字段
     */
    private void resParamHandle(MonitorLog monitorLog, Object result, MonitorLogDo monitorLogDo) {
        if (Objects.isNull(result) || Objects.isNull(monitorLog) || Objects.isNull(monitorLogDo)) {
            log.info("monitorInnerParam||monitorLog is null");
            return;
        }
        monitorLogDo.setResData(MonitorContext.removeUnencryptedResData());
        monitorLogDo.setResResult(JSON.toJSONString(result));
        if (result instanceof GatewayResponseVo || GatewayResponseVo.class.equals(monitorLog.resultClassType())) {
            //GatewayResponseVo类型处理
            assert result instanceof GatewayResponseVo;
            GatewayResponseVo commonResp = (GatewayResponseVo) result;
            monitorLogDo.setResCode(commonResp.getCode());
            monitorLogDo.setResMsg(commonResp.getMessage());
        } else {
            //默认处理方式
            JSONObject dataObj = JSON.parseObject(JSON.toJSONString(result));
            String code = Objects.isNull(dataObj.get(monitorLog.monitorCodeFieldName())) ? null : dataObj.get(monitorLog.monitorCodeFieldName()).toString();
            monitorLogDo.setResCode(code);
            String msg = Objects.isNull(dataObj.get(monitorLog.monitorMsgFieldName())) ? null : dataObj.get(monitorLog.monitorMsgFieldName()).toString();
            monitorLogDo.setResMsg(msg);
        }
    }
}
