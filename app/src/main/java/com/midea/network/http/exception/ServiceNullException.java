package com.midea.network.http.exception;

/**
 * 服务器返回的Respons为空
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class ServiceNullException extends RuntimeException {

    public ServiceNullException(String msg) {
        super(msg);
    }

}