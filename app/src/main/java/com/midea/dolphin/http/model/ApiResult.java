package com.midea.dolphin.http.model;

/**
 * 提供的默认的标准返回
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class ApiResult<T> implements IApiResult<T> {

    public boolean success;

    public String errCode;

    public String errMsg;

    public long sysTime;

    public T data;

    private boolean isFromCache;

    @Override
    public boolean isResultSuccess() {
        return success;
    }

    @Override
    public T getResultData() {
        return data;
    }

    @Override
    public String getReultCode() {
        return errCode;
    }

    @Override
    public String getResultMessage() {
        return errMsg;
    }

    @Override
    public long getResultTime() {
        return sysTime;
    }

    @Override
    public void isFromCache(boolean from) {
        isFromCache = from;
    }

    public boolean isFromCache() {
        return isFromCache;
    }

    @Override
    public String toString() {
        return "ApiResult{" +
                "success=" + success +
                ", errCode='" + errCode + '\'' +
                ", errMsg='" + errMsg + '\'' +
                ", sysTime=" + sysTime +
                ", data=" + data +
                '}';
    }
}
