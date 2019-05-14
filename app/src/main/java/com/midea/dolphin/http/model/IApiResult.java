package com.midea.dolphin.http.model;

/**
 * 后台返回数据顶层接口,方便调用者使用自己的Result去定义公共返回
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public interface IApiResult<T> {

    /**
     * 接口请求成功的标识,用于判断 getResultData()的时机
     * 一般后台返回 code = 0 || succes = true 表示成功
     * 根据自己的实际业务去复写
     */
    boolean isResultSuccess();

    /**
     * 获取实际的业务对象
     */
    T getResultData();

    /**
     * 获取接口返回的编码
     */
    String getReultCode();

    /**
     * 获取接口返回的信息
     */
    String getResultMessage();

    /**
     * 获取返回的接口时间
     */
    long getResultTime();

    /**
     * 是否来源于缓存
     * @param from true 是; false 否
     */
    void isFromCache(boolean from);


}
