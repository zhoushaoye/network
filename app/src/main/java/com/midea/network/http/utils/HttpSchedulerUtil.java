
package com.midea.network.http.utils;


import com.midea.network.http.transformer.SchedulersTransformer;

import io.reactivex.ObservableTransformer;

/**
 * 线程调度
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class HttpSchedulerUtil {

    public static <T> ObservableTransformer<T, T> _main() {
        return new SchedulersTransformer<>(SchedulerType._main);
    }

    public static <T> ObservableTransformer<T, T> _io() {
        return new SchedulersTransformer<>(SchedulerType._io);
    }


    public static <T> ObservableTransformer<T, T> _io_main() {
        return new SchedulersTransformer<>(SchedulerType._io_main);
    }

    public static <T> ObservableTransformer<T, T> _io_io() {
        return new SchedulersTransformer<>(SchedulerType._io_io);
    }

    /**
     * 线程调度
     *
     * @author zhoudingjun
     * @version 1.0
     * @since 2019/5/14
     */
    public enum  SchedulerType {

        //订阅发生在主线程
        _main,
        //订阅发生在io线程
        _io,
        //处理在io线程,订阅发生在主线程
        _io_main,
        // 处理在io线程,订阅也发生在io线程
        _io_io,
    }
}
