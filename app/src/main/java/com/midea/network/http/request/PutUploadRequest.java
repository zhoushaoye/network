package com.midea.network.http.request;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Put方式的上传
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class PutUploadRequest extends BaseBodyRequest<PutUploadRequest> {

    public PutUploadRequest(String url) {
        super(url);
    }

    @Override
    protected Observable<ResponseBody> createRequest() {
        if (this.mStringContent != null) {
            RequestBody body = RequestBody.create(mMediaTypeContent, this.mStringContent);
            return build().getApiService().putBody(getHttpHeaders().getHeadersMap(),getRequestUrl(), body);
        } else {
            return build().getApiService().putBody(getHttpHeaders().getHeadersMap(),getRequestUrl(), uploadFilesWithBody());
        }
    }

}
