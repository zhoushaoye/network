
package com.midea.network.http.callback;


import com.midea.network.http.exception.HttpException;

/**
 * 上传下载进度回调(会回调到UI线程,可以直接操作UI)
 *
 * @author zhudinogjun
 * @version 1.0
 * @since 2019/5/13
 */
public interface IProgressCallBack  {

    /**
     * 上传开始
     */
    void onStart();

    /**
     * 回调进度
     *
     * @param bytesWritten  当前读取响应体字节长度
     * @param contentLength 总长度
     */
    void onProgress(String path, long bytesWritten, long contentLength);

    /**
     * 上传完成
     */
    void onComplete(String path);

    /**
     * 上传出现错误
     */
    void onError(HttpException e);

}
