package com.midea.network.http.exception;

import android.accounts.NetworkErrorException;
import android.net.ParseException;

import com.google.gson.JsonParseException;
import com.midea.network.http.utils.HttpLogUtil;


import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;

import java.io.NotSerializableException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLHandshakeException;


/**
 * 统一进行了异常的转换处理,归集了所有的异常
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class HttpException extends Exception {

    private static final String PREFIX = "E_";

    //其它错误
    public static final String ERROR_UNKNOWN = PREFIX+ 0x1001;

    //解析错误
    public static final String ERROR_PARSE = PREFIX+ 0x1002;

    //网络连接错误
    public static final String ERROR_CONNECT = PREFIX+ 0x1003;

    //证书出错
    public static final String ERROR_SSL = PREFIX+ 0x1004;

    //超时
    public static final String ERROR_TIMEOUT = PREFIX+ 0x1005;

    //类型转换错误
    public static final String ERROR_ClASSCAST = PREFIX+ 0x1006;

    //未知主机错误
    public static final String ERROR_UNKNOWNHOST = PREFIX+ 0x1007;

    //空指针错误
    public static final String ERROR_NULLPOINT = PREFIX+ 0x1008;

    //无网络
    public static final String ERROR_NETWORK = PREFIX+ 0x1009;

    //返回为null
    public static final String ERROR_SERVICENULL = PREFIX+ 0x1010;

    //Rxtimeout
    public static final String ERROR_RX_TIMEOUT = PREFIX+ 0x1011;

    private  String mCode = ERROR_UNKNOWN;

    private  String mMessage = "";

    public HttpException(String mssage) {
        super(mssage);
        this.mMessage = mssage;
        if (HttpLogUtil.isDebug) {
            HttpLogUtil.e("http onError ... " + " code : " + mCode + " message: " + mssage);
        }
    }

    public HttpException(Throwable throwable, String code) {
        super(throwable);
        this.mCode = code;
        this.mMessage = throwable.getMessage();
        if (HttpLogUtil.isDebug) {
            HttpLogUtil.e("http onError ... " + " code : " + mCode + " message: " + throwable.getMessage());
            throwable.printStackTrace();
        }
    }

    public String getCode() {
        return mCode;
    }

    //对归集的异常进行分类,方便后续的调用者进行区分
    public static HttpException handleException(Throwable throwable) {
        HttpException httpException;
        if (throwable instanceof retrofit2.HttpException) {
            //Retrofit2返回的异常,主要包含Respons的各种异常
            retrofit2.HttpException exception = (retrofit2.HttpException) throwable;
            httpException = new HttpException(exception, exception.code() + "");
        } else if (throwable instanceof ServiceException) {
            //自定义的服务器返回异常
            ServiceException resultException = (ServiceException) throwable;
            httpException = new HttpException(resultException, resultException.getErrCode());
        } else if (throwable instanceof ServiceNullException) {
            //自定义的服务器返回null异常
            ServiceNullException resultException = (ServiceNullException) throwable;
            httpException = new HttpException(resultException,ERROR_SERVICENULL);
        } else if (throwable instanceof JsonParseException
                || throwable instanceof JSONException
                || throwable instanceof NotSerializableException
                || throwable instanceof ParseException) {
            httpException = new HttpException(throwable, ERROR_PARSE);
        } else if (throwable instanceof ConnectException) {
            httpException = new HttpException(throwable, ERROR_CONNECT);
        } else if (throwable instanceof SSLHandshakeException) {
            httpException = new HttpException(throwable, ERROR_SSL);
        } else if (throwable instanceof ConnectTimeoutException
                || throwable instanceof SocketTimeoutException) {
            httpException = new HttpException(throwable, ERROR_TIMEOUT);
        } else if (throwable instanceof ClassCastException) {
            httpException = new HttpException(throwable, ERROR_ClASSCAST);
        } else if (throwable instanceof UnknownHostException) {
            httpException = new HttpException(throwable, ERROR_UNKNOWNHOST);
        } else if (throwable instanceof NullPointerException) {
            httpException = new HttpException(throwable, ERROR_NULLPOINT);
        } else if (throwable instanceof NetworkErrorException) {
            httpException = new HttpException(throwable, ERROR_NETWORK);
        } else if (throwable instanceof TimeoutException) {
            httpException = new HttpException(throwable, ERROR_RX_TIMEOUT);
        } else {
            httpException = new HttpException(throwable, ERROR_UNKNOWN);
        }
        return httpException;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }

}