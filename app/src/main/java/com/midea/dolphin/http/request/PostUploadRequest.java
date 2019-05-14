package com.midea.dolphin.http.request;


import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Post方式进行上传
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class PostUploadRequest extends BaseBodyRequest<PostUploadRequest> {

    public PostUploadRequest(String url) {
        super(url);
    }

    protected Observable<ResponseBody> createRequest() {
        build();
        if (this.mStringContent != null) {
            //上传的文本内容
            RequestBody body = RequestBody.create(mMediaTypeContent, this.mStringContent);
            return getApiService().postBody(getHttpHeaders().getHeadersMap(),getRequestUrl(), body);
        } else if (this.mJsonStr != null) {
            // Json串
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), this.mJsonStr);
            return getApiService().postJson(getHttpHeaders().getHeadersMap(),getRequestUrl(), body);
        } else if (this.mBytesContent != null) {
            //上传的字节数据
            RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), this.mBytesContent);
            return getApiService().postBody(getHttpHeaders().getHeadersMap(),getRequestUrl(), body);
        }
        if (mCurrentUploadType == UploadType.PART) {
            //part方式上传
            return getApiService().uploadFiles(getHttpHeaders().getHeadersMap(),getRequestUrl(), uploadFilesWithParts());
        } else {
            //body方式上传
            return getApiService().uploadFiles(getHttpHeaders().getHeadersMap(),getRequestUrl(), uploadFilesWithBodys());
        }
    }
}
