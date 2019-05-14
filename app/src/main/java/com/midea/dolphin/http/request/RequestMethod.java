package com.midea.dolphin.http.request;


import com.midea.dolphin.http.model.RequestMethodModel;

/**
 * 请求方法
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class RequestMethod implements IRequestMethod {

    @Override
    public Request doPost(String url) {
        return new Request(url, RequestMethodModel.POST);
    }

    @Override
    public Request doGet(String url) {
        return new Request(url,RequestMethodModel.GET);
    }

    @Override
    public Request doPut(String url) {
        return new Request(url,RequestMethodModel.PUT);
    }

    @Override
    public Request doDelete(String url) {
        return new Request(url,RequestMethodModel.DELET);
    }
}
