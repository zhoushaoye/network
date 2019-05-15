package com.midea.dolphin.base.basefragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.midea.dolphin.base.view.IStatusView;
import com.midea.dolphin.base.view.StatusView;
import com.midea.dolphin.base.widget.IStatusViewProvider;
import com.midea.dolphin.base.mvp.IView;
import com.midea.dolphin.base.rx.RxLifecycleCompositor;
import com.midea.dolphin.base.view.LoadViewBuilder;
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle;
import com.trello.rxlifecycle3.LifecycleProvider;
import com.trello.rxlifecycle3.LifecycleTransformer;


import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.lifecycle.Lifecycle;
import io.reactivex.disposables.Disposable;

/**
 * 基础Fragment弹窗
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public abstract class BaseDialogFragment extends AppCompatDialogFragment implements IView, IStatusViewProvider {

    /**
     * 绑定Activity生命周期提供器
     */
    private final LifecycleProvider<Lifecycle.Event> provider = AndroidLifecycle.createLifecycleProvider(this);

    /**
     * 状态View
     */
    private IStatusView mStatusView;

    /**
     * 内容View
     */
    private View contentView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutRes = getLayout();
        if (layoutRes != 0) {
            View contentView = inflater.inflate(layoutRes, null);
            return covertContentView(contentView);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * 转换内容View，对内容View增加或删除等操作
     *
     * @param contentView 内容View
     * @return Fragment的最终展示View
     */
    protected View covertContentView(View contentView) {
        this.contentView = contentView;
        Activity activity = getActivity();
        LoadViewBuilder builder = new LoadViewBuilder(activity);
        builder.hasLoadStatus(false);
        onChangeContentView(builder);
        if (builder.isHasLoadStatus()) {
            mStatusView = builder.getStatusView();
            if (mStatusView == null) {
                mStatusView = new StatusView(activity);
            }
            mStatusView.setStatusViewProvider(this);
        }
        builder.statusView(mStatusView);
        return builder.build(contentView);
    }

    /**
     * 由子类覆盖该方法，可改变{@link LoadViewBuilder}的属性
     */
    protected void onChangeContentView(LoadViewBuilder builder) {

    }

    /**
     * 显示加载状态View
     */
    @Override
    public void showLoadingView() {
        if (mStatusView != null) {
            mStatusView.showLoading();
        }
    }

    /**
     * 显示内容View
     */
    @Override
    public void showContentView() {
        if (mStatusView != null) {
            mStatusView.showContent();
        }
    }

    /**
     * 显示加载错误View(网络失败等)
     */
    @Override
    public void showErrorView() {
        if (mStatusView != null) {
            mStatusView.showError();
        }
    }

    /**
     * 显示空白View(无数据)
     */
    @Override
    public void showEmptyView() {
        if (mStatusView != null) {
            mStatusView.showEmpty();
        }
    }

    @Override
    public View getLoadingView(Context context, ViewGroup container) {
        return null;
    }

    @Override
    public final View getContentView(Context context, ViewGroup container) {
        return contentView;
    }

    @Override
    public View getEmptyView(Context context, ViewGroup container) {
        return null;
    }

    @Override
    public View getErrorView(Context context, ViewGroup container) {
        return null;
    }

    public IStatusView getStatusView() {
        return mStatusView;
    }

    /**
     * 把Rx的Disposable与Activity生命周期绑定
     *
     * @param disposable Disposable
     */
    public final void bindDisposable(Disposable disposable) {
        RxLifecycleCompositor.getLifecycleObserver(getLifecycle())
                .addDisposable(disposable);
    }

    /**
     * 从Fragment生命周期绑定的Disposable中移除Disposable
     *
     * @param disposable Disposable
     */
    public final void removeDisposable(Disposable disposable) {
        RxLifecycleCompositor.getLifecycleObserver(getLifecycle())
                .removeDisposable(disposable);
    }

    @Override
    public final <T> LifecycleTransformer<T> bindUntilDestroy() {
        return provider.bindUntilEvent(Lifecycle.Event.ON_DESTROY);
    }

    @Override
    public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull Lifecycle.Event event) {
        return provider.bindUntilEvent(event);
    }

    /**
     * 内容layout资源（必须覆盖该方法）
     *
     * @return layout资源id
     */
    @LayoutRes
    protected abstract int getLayout();
}
