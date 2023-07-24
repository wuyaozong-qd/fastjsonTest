package com.example.fastjsontest.controller;

import com.alibaba.fastjson.JSON;
import com.example.fastjsontest.aspect.MonitorLog;
import com.example.fastjsontest.vo.GatewayRequestVo;
import com.example.fastjsontest.vo.GatewayResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/test/")
public class TestController {

    @RequestMapping("handle")
    @ResponseBody
    @MonitorLog(monitorFieldName = "outOrderNo")
    public GatewayResponseVo handle(@RequestBody GatewayRequestVo vo, @RequestHeader Map<String, String> headers) {
        log.info("GatewayRequestVo:" + JSON.toJSONString(vo));
        log.info("headers:" + JSON.toJSONString(headers));
        return new GatewayResponseVo();
    }
}
