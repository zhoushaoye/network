
package com.midea.network.http.transformer;


import com.midea.network.http.utils.HttpSchedulerUtil.SchedulerType;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.midea.network.http.utils.HttpSchedulerUtil.SchedulerType._io_main;
import static com.midea.network.http.utils.HttpSchedulerUtil.SchedulerType._main;

/**
 * 线程切换调度
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class SchedulersTransformer<T> implements ObservableTransformer<T, T> {

    private SchedulerType mSchedulerType;

    public SchedulersTransformer(boolean isSync) {
        mSchedulerType = getSchedulerType(isSync);
    }

    public SchedulersTransformer(SchedulerType schedulerType) {
        mSchedulerType = schedulerType;
    }

    private SchedulerType getSchedulerType(boolean isSync) {
        if (isSync) {
            //同步请求
            return _main;
        } else {
            //异步请求
            return _io_main;
        }
    }

    @Override
    public ObservableSource<T> apply(Observable<T> upstream) {
        switch (mSchedulerType) {
            case _main:
                return upstream
                        .observeOn(AndroidSchedulers.mainThread());
            case _io:
                return upstream;
            case _io_main:
                return upstream
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            case _io_io:
                return upstream
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io());
            default:
                break;
        }
        return upstream;
    }
}
