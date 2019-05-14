package com.midea.dolphin.base.rx;

import android.util.ArrayMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

/**
 * Rx生命周期绑定器
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public class RxLifecycleCompositor {

    private static ArrayMap<Lifecycle, RxLifecycleObserver> observers = new ArrayMap<>();


    private RxLifecycleCompositor() {
    }

    /**
     * 获取生命周期监听器
     *
     * @param activity AppCompatActivity
     * @return RxLifecycleObserver
     */
    public static RxLifecycleObserver getLifecycleObserver(AppCompatActivity activity) {
        return getLifecycleObserver(activity.getLifecycle());
    }

    /**
     * 获取生命周期监听器
     *
     * @param fragment Fragment
     * @return RxLifecycleObserver
     */
    public static RxLifecycleObserver getLifecycleObserver(Fragment fragment) {
        return getLifecycleObserver(fragment.getLifecycle());
    }

    /**
     * 获取生命周期监听器
     *
     * @param lifecycle Lifecycle
     * @return RxLifecycleObserver
     */
    public static RxLifecycleObserver getLifecycleObserver(Lifecycle lifecycle) {
        RxLifecycleObserver observer = observers.get(lifecycle);
        if (observer == null) {
            observer = new RxLifecycleObserver(lifecycle);
            observers.put(lifecycle, observer);
        }
        return observer;
    }

    /**
     * 移除生命周期监听器
     *
     * @param activity AppCompatActivity
     * @return RxLifecycleObserver
     */
    public static void removeLifecycleObserver(AppCompatActivity activity) {
        removeLifecycleObserver(activity.getLifecycle());
    }

    /**
     * 移除生命周期监听器
     *
     * @param fragment Fragment
     * @return RxLifecycleObserver
     */
    public static void removeLifecycleObserver(Fragment fragment) {
        removeLifecycleObserver(fragment.getLifecycle());
    }

    /**
     * 移除生命周期监听器
     *
     * @param lifecycle Lifecycle
     * @return RxLifecycleObserver
     */
    public static void removeLifecycleObserver(Lifecycle lifecycle) {
        RxLifecycleObserver observer = observers.get(lifecycle);
        if (observer != null) {
            observers.remove(lifecycle);
        }
    }

}
