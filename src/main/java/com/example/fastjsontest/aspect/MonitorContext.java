package com.example.fastjsontest.aspect;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MonitorContext {
    private static final ThreadLocal<Boolean> OPEN_MONITOR_LOG = new ThreadLocal<>();
    private static final ThreadLocal<String> UNENCRYPTED_REQ_DATA = new ThreadLocal<>();
    private static final ThreadLocal<String> UNENCRYPTED_RES_DATA = new ThreadLocal<>();

    public static void setUnencryptedReqData(String data) {
        try {
            if (!Boolean.TRUE.equals(OPEN_MONITOR_LOG.get())) {
                return;
            }
            UNENCRYPTED_REQ_DATA.set(data);
        } catch (Exception e) {
            log.error("setUnencryptedReqData", e);
        }
    }

    public static String removeUnencryptedReqData() {
        try {
            return UNENCRYPTED_REQ_DATA.get();
        } catch (Exception e) {
            log.error("removeUnencryptedReqData", e);
            return null;
        } finally {
            UNENCRYPTED_REQ_DATA.remove();
        }
    }

    public static void setUnencryptedResData(String data) {
        try {
            if (!Boolean.TRUE.equals(OPEN_MONITOR_LOG.get())) {
                return;
            }
            UNENCRYPTED_RES_DATA.set(data);
        } catch (Exception e) {
            log.error("setUnencryptedResData", e);
        }
    }

    public static String removeUnencryptedResData() {
        try {
            return UNENCRYPTED_RES_DATA.get();
        } catch (Exception e) {
            log.error("removeUnencryptedResData", e);
            return null;
        } finally {
            UNENCRYPTED_RES_DATA.remove();
        }
    }

    public static void setOpenMonitorLog(Boolean openMonitorLog) {
        try {
            OPEN_MONITOR_LOG.set(openMonitorLog);
        } catch (Exception e) {
            log.error("setOpenMonitorLog", e);
        }
    }

    public static void removeOpenMonitorLog() {
        try {
            OPEN_MONITOR_LOG.remove();
        } catch (Exception e) {
            log.error("removeOpenMonitorLog", e);
        }
    }
}
