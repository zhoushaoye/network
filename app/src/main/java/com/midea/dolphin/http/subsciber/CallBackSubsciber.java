package com.midea.dolphin.http.subsciber;

import android.content.Context;

import com.midea.dolphin.http.callback.IHttpCallBack;
import com.midea.dolphin.http.callback.ProgressHttpCallBack;
import com.midea.dolphin.http.exception.HttpException;
import com.midea.dolphin.http.exception.ServiceNullException;
import com.midea.dolphin.http.model.IApiResult;
import com.midea.dolphin.http.utils.HttpLogUtil;


import io.reactivex.annotations.NonNull;


/**
 * 使用HttpCallBack进行回调,简化调用者的处理
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class CallBackSubsciber<T> extends BaseSubscriber<T> {

    private IHttpCallBack<T> mCallBack;


    public CallBackSubsciber(Context context) {
        super(context);
    }

    public CallBackSubsciber(Context context,IHttpCallBack<T> callBack) {
        super(context);
        mCallBack = callBack;
        if (callBack instanceof ProgressHttpCallBack) {
            ((ProgressHttpCallBack) callBack).subscription(this);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mCallBack != null) {
            mCallBack.onStart();
        }
    }

    @Override
    public void onError(HttpException e) {
        if (mCallBack != null) {
            mCallBack.onError(e);
        }
    }

    @Override
    public void onNext(@NonNull T t) {
        super.onNext(t);
        if (t == null) {
            onError(HttpException.handleException(new ServiceNullException("Http respons is null ...")));
        } else {
            if (t instanceof IApiResult) {
                IApiResult<T> apiResult = (IApiResult<T>) t;
                if (mCallBack != null) {
                    mCallBack.onSuccess(apiResult);
                }else {
                    HttpLogUtil.e("Http callback is null  ...");
                }
            } else {
                onError(HttpException.handleException(
                        new HttpException("Not found zhe allow result map,you must implement IApiResult<T> ..."+t)));
            }

        }
    }

    @Override
    public void onComplete() {
        super.onComplete();
        if (mCallBack != null) {
            mCallBack.onComplete();
        }
    }

}
