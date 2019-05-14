package com.midea.dolphin.base.rx;



import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Rx生命周期监听器，主要用于管理Disposable
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public class RxLifecycleObserver implements LifecycleObserver {

    private CompositeDisposable mDisposables = new CompositeDisposable();

    private Lifecycle mLifecycle;

    RxLifecycleObserver(Lifecycle lifecycle) {
        this.mLifecycle = lifecycle;
        mLifecycle.addObserver(this);
    }

    /**
     * 添加Rx的Disposable，将会在onDestroy的时候自动销毁
     *
     * @param disposable Disposable
     */
    public void addDisposable(Disposable disposable) {
        mDisposables.add(disposable);
    }

    /**
     * 移除已添加的Rx Disposable，将会在onDestroy的时候自动销毁
     *
     * @param disposable Disposable
     */
    public void removeDisposable(Disposable disposable) {
        mDisposables.remove(disposable);
    }

    /**
     * 清空已绑定的Disposable
     */
    public void clearDisposable() {
        mDisposables.clear();
    }

    /**
     * 销毁，对应View的onDestroy
     *
     * @param owner 生命周期主体
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(@NonNull LifecycleOwner owner) {
        mDisposables.dispose();
        RxLifecycleCompositor.removeLifecycleObserver(mLifecycle);
    }
}
