package com.midea.dolphin.base.basepresenter;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.midea.dolphin.base.mvp.IPresenter;
import com.midea.dolphin.base.mvp.IView;
import com.midea.dolphin.base.rx.RxLifecycleCompositor;

import io.reactivex.disposables.Disposable;

/**
 * 基础Presenter，所有的Presenter都集成与此Presenter
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public class BasePresenter<T extends IView> implements IPresenter {

    protected T mView;

    public BasePresenter(T view) {
        this.mView = view;
        Lifecycle lifecycle = mView.getLifecycle();
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
    }

    @Override
    public void onLifecycleChanged(@NonNull LifecycleOwner owner, @NonNull Lifecycle.Event event) {
    }

    @Override
    public final void bindDisposable(Disposable disposable) {
        RxLifecycleCompositor.getLifecycleObserver(mView.getLifecycle())
                .addDisposable(disposable);
    }

    @Override
    public final void removeDisposable(Disposable disposable) {
        RxLifecycleCompositor.getLifecycleObserver(mView.getLifecycle())
                .removeDisposable(disposable);
    }

    @CallSuper
    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
    }
}
