package com.midea.dolphin.http.function;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.midea.dolphin.http.exception.ServiceNullException;
import com.midea.dolphin.http.model.IApiResult;
import com.midea.dolphin.http.utils.HttpLogUtil;


import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;


/**
 * 解析返回的ResponseBody --> ApiResult<T>
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class ResponseBodyFunction<T> implements Function<ResponseBody, IApiResult<T>> {

    private final Gson mGson;

    private final TypeAdapter<IApiResult<T>> mTypeAdapter;

    public ResponseBodyFunction(@NonNull TypeAdapter<IApiResult<T>> adapter, Gson gson) {
        mGson = gson;
        mTypeAdapter = adapter;
    }

    @Override
    public IApiResult<T> apply(@NonNull ResponseBody responseBody) {
        IApiResult<T> apiResult;
        JsonReader jsonReader = mGson.newJsonReader(responseBody.charStream());
        long contentLength = responseBody.contentLength();
        HttpLogUtil.d("contentLength: " + contentLength);
        try {
            if (contentLength != 0) {
                apiResult = mTypeAdapter.read(jsonReader);
                if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                    throw new JsonIOException("JSON document was not fully consumed.");
                }
            } else {
                throw new ServiceNullException("Respons is null ...");
            }
        } catch (Exception e) {
            apiResult =  getApiResult();
            e.printStackTrace();
        } finally {
            responseBody.close();
        }
        return apiResult;
    }

    /**
     * 防止服务器无返回,又不处理RxJava中的onError而导致的问题
     */
    private IApiResult<T> getApiResult() {
        return new IApiResult<T>() {
            @Override
            public boolean isResultSuccess() {
                return false;
            }

            @Override
            public T getResultData() {
                return null;
            }

            @Override
            public String getReultCode() {
                return null;
            }

            @Override
            public String getResultMessage() {
                return null;
            }

            @Override
            public long getResultTime() {
                return 0;
            }

            @Override
            public void isFromCache(boolean from) {

            }
        };
    }

}
