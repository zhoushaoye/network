package com.midea.dolphin.http.callback;

import okhttp3.Request;
import okhttp3.Response;

/**
 * 接口原始信息回调
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public interface IHttpResponseCallBack {

    /**
     * 返回当前请求的各个数据Request,Response,
     * @param request 请求体
     * @param response 响应
     * @param json  响应结果(不一定是标准的Json串)
     */
    void onResponse(Request request, Response response, String json);

}
