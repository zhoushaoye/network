package com.midea.network.http.request;

/**
 * 请求接口方法
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public interface IRequestMethod {

    /**
     * post 请求
     * @param url api地址
     */
    Request doPost(String url);

    /**
     * get请求
     * @param url api地址
     */
    Request doGet(String url);

    /**
     * put请求
     * @param url api地址
     */
    Request doPut(String url);

    /**
     * delete请求
     * @param url api地址
     */
    Request doDelete(String url);

}
