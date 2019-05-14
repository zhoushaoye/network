package com.midea.dolphin.base.rx;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * 自定义封装RxBus
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public class RxBus {

    /**
     * 订阅的发布者
     */
    private static volatile Subject<Object> bus;

    private static Subject<Object> getBus() {
        if (bus == null) {
            synchronized (RxBus.class) {
                if (bus == null) {
                    // toSerialized保证线程安全
                    bus = PublishSubject.create().toSerialized();
                }
            }
        }
        return bus;
    }


    /**
     * 发送事件
     *
     * @param object 发送事件数据
     */
    public static void post(Object object) {
        getBus().onNext(object);
    }

    /**
     * 根据观察的对象类型获取到RxBus事件
     *
     * @param clazz 观察对象类型
     * @return RxBusEvent
     */
    public static <T> RxBusEvent<T> with(final Class<T> clazz) {
        Observable<T> observable = getBus().ofType(clazz);
        return new RxBusEvent<>(observable);
    }
}
