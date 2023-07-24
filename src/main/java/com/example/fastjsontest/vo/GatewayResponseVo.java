package com.example.fastjsontest.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GatewayResponseVo<T> implements Serializable {
    private String logId;
    private String code;
    private String message;
    private String data;
}
