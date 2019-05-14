package com.midea.network.http.function;




import com.midea.network.http.utils.HttpLogUtil;

import org.apache.http.conn.ConnectTimeoutException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

/**
 * 网络请求错误重试条件
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class HttpRetryFunction implements Function<Observable<? extends Throwable>, Observable<?>> {

    //重试次数
    private int mCount;

    //延迟
    private long mDelay = 500;

    public HttpRetryFunction(int count) {
        this.mCount = count;
    }

    @Override
    public Observable<?> apply(@NonNull Observable<? extends Throwable> observable) {
        return observable.zipWith(Observable.range(1, mCount + 1), new BiFunction<Throwable, Integer, Wrapper>() {
            @Override
            public Wrapper apply(@NonNull Throwable throwable, @NonNull Integer integer) {
                return new Wrapper(throwable, integer);
            }
        }).flatMap(new Function<Wrapper, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(@NonNull Wrapper wrapper) {
                if (wrapper.index > 1) {
                    HttpLogUtil.i("重试次数：" + (wrapper.index));
                }
                if ((wrapper.throwable instanceof ConnectException
                        || wrapper.throwable instanceof SocketTimeoutException
                        || wrapper.throwable instanceof ConnectTimeoutException
                        || wrapper.throwable instanceof UnknownHostException
                        || wrapper.throwable instanceof TimeoutException)
                        && wrapper.index < mCount + 1) {
                    return Observable.timer(mDelay, TimeUnit.MILLISECONDS);

                }
                return Observable.error(wrapper.throwable);
            }
        });
    }

    private class Wrapper {

        private int index;

        private Throwable throwable;

        public Wrapper(Throwable throwable, int index) {
            this.index = index;
            this.throwable = throwable;
        }
    }

}
