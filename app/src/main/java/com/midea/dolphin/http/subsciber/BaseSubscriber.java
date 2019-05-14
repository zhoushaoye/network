package com.midea.dolphin.http.subsciber;

import android.content.Context;


import com.midea.dolphin.http.exception.HttpException;
import com.midea.dolphin.http.utils.HttpLogUtil;
import com.midea.dolphin.http.utils.HttpUtil;

import java.lang.ref.WeakReference;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;

/**
 *  订阅者基类
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public abstract class BaseSubscriber<T> extends DisposableObserver<T> {

    private WeakReference<Context> mContextWeakReference;

    @Override
    protected void onStart() {
        HttpLogUtil.i("Http onStart...");
        if (mContextWeakReference != null && mContextWeakReference.get() != null
                && !HttpUtil.isNetworkAvailable(mContextWeakReference.get())) {
            HttpLogUtil.e("no network,please check your network setting...");
        }
    }

    public BaseSubscriber(Context context) {
        if (context != null) {
            mContextWeakReference = new WeakReference<>(context);
        }
    }

    @Override
    public void onNext(@NonNull T t) {
        HttpLogUtil.i("Http onNext...");
    }

    @Override
    public final void onError(Throwable e) {
        HttpLogUtil.i("Http onError...");
        onError(HttpException.handleException(e));
        onComplete();
    }

    @Override
    public void onComplete() {
        HttpLogUtil.i("Http onComplete...");
    }


    public abstract void onError(HttpException e);

}
