package com.midea.network.http.exception;

/**
 * 服务器各种异常Code,Message
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class ServiceException extends RuntimeException {

    private String mErrCode;

    private String mMessage;

    public ServiceException(String errCode, String msg) {
        super(msg);
        this.mErrCode = errCode;
        this.mMessage = msg;
    }

    public ServiceException(int errCode, String msg) {
        this(errCode+"",msg);
    }

    public String getErrCode() {
        return mErrCode;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}