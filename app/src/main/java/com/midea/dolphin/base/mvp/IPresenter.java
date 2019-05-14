package com.midea.dolphin.base.mvp;


import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import io.reactivex.disposables.Disposable;

/**
 * MVP结构基础Presenter接口
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public interface IPresenter extends LifecycleObserver {

    /**
     * 创建，对应View的onCreate
     *
     * @param owner 生命周期主体
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void onCreate(@NonNull LifecycleOwner owner);

    /**
     * 对应View的onResume
     *
     * @param owner 生命周期主体
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume(@NonNull LifecycleOwner owner);

    /**
     * 生命周期切换，对应View的生命周期变换，会调用该方法
     *
     * @param owner 生命周期主体
     * @param event {@link Lifecycle}生命周期
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onLifecycleChanged(@NonNull LifecycleOwner owner, @NonNull Lifecycle.Event event);

    /**
     * 销毁，对应View的onDestroy
     *
     * @param owner 生命周期主体
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy(@NonNull LifecycleOwner owner);

    /**
     * 绑定rx的Disposable，当View destroy的时候，将会取消该Disposable
     *
     * @param disposable 需要绑定的Disposable
     */
    void bindDisposable(Disposable disposable);

    /**
     * 从绑定的Disposable列表中移除Disposable
     *
     * @param disposable 需要移除的Disposable
     */
    void removeDisposable(Disposable disposable);

}
