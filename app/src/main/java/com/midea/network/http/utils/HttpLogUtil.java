package com.midea.network.http.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 日志打印工具
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class HttpLogUtil {

    public static boolean isDebug;

    private static final Level INFO = Level.INFO;

    private static final Level SEVERE = Level.SEVERE;

    private static final String TAG = "DolphinHttp";


    public static Logger logger() {
        return Logger.getLogger(TAG);
    }

    public static void d(String content) {
        if (isDebug) {
            logger().log(INFO, content);
        }
    }

    public static void d(String content, Throwable tr) {
        if (isDebug) {
            logger().log(INFO, content, tr);
        }
    }

    public static void e(String content) {
        if (isDebug) {
            logger().log(SEVERE, content);
        }
    }

    public static void e(Throwable e) {
        if (isDebug) {
            logger().log(SEVERE, "", e);
        }
    }

    public static void e(String content, Throwable tr) {
        if (isDebug) {
            logger().log(SEVERE, content, tr);
        }
    }

    public static void i(String content) {
        if (isDebug) {
            logger().log(INFO, content);
        }
    }

    public static void i(String content, Throwable tr) {
        if (isDebug) {
            logger().log(INFO, content, tr);
        }
    }

}
