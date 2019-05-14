package com.midea.dolphin.base.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.midea.dolphin.R;
import com.midea.dolphin.base.IStatusView;
import com.midea.dolphin.base.StatusView;
import com.midea.dolphin.base.StatusViewProvider;
import com.midea.dolphin.base.mvp.IView;
import com.midea.dolphin.base.rx.RxLifecycleCompositor;
import com.midea.dolphin.base.view.ContentViewBuilder;
import com.midea.dolphin.base.widget.StatusToolbar;
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle;
import com.trello.rxlifecycle3.LifecycleProvider;
import com.trello.rxlifecycle3.LifecycleTransformer;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TintTypedArray;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import io.reactivex.disposables.Disposable;

/**
 * 基础Fragment，所有的Fragment都集成与此Fragment
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public abstract class BaseFragment extends Fragment implements IView, StatusViewProvider {

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

    /**
     * status+toolbar
     */
    private StatusToolbar mStatusToolbar;

    private ContentViewBuilder mBuilder;

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
        mBuilder = new ContentViewBuilder(activity);
        int layout = getStatusToolbarLayout();
        mBuilder.hasHeaderBar(layout != 0);
        onChangeContentView(mBuilder);
        if (mBuilder.isHasHeaderBar()) {
            mStatusToolbar = mBuilder.getStatusToolbar();
            if (mStatusToolbar == null) {
                if (layout != 0) {
                    mStatusToolbar = (StatusToolbar) LayoutInflater.from(activity).inflate(layout, null);
                    mBuilder.statusToolbar(mStatusToolbar);
                } else {
                    mStatusToolbar = new StatusToolbar(activity);
                }
            }
        }
        if (mBuilder.isHasLoadStatus()) {
            mStatusView = mBuilder.getStatusView();
            if (mStatusView == null) {
                mStatusView = new StatusView(activity);
            }
            mStatusView.setStatusViewProvider(this);
        }
        mBuilder.statusView(mStatusView);
        initToolbar();
        return mBuilder.build(contentView);
    }

    /**
     * 初始化ActionBar
     */
    @SuppressLint("RestrictedApi")
    private void initToolbar() {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            if (toolbar.getNavigationIcon() == null) {
                TintTypedArray a = TintTypedArray.obtainStyledAttributes(toolbar.getContext(), null, R.styleable.ActionBar, R.attr.actionBarStyle, 0);
                Drawable defaultNavigationIcon = a.getDrawable(R.styleable.ActionBar_homeAsUpIndicator);
                if (defaultNavigationIcon != null) {
                    toolbar.setNavigationIcon(defaultNavigationIcon);
                }
                a.recycle();
            }
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseFragment.this.onNavigationBackPressed();
                }
            });
            Activity activity = getActivity();
            if (activity != null && !TextUtils.isEmpty(activity.getTitle())
                    && TextUtils.isEmpty(toolbar.getTitle())) {
                toolbar.setTitle(activity.getTitle());
            }
        }
    }

    /**
     * 获取Toolbar
     *
     * @return Toolbar
     */
    protected final Toolbar getToolbar() {
        if (mStatusToolbar != null) {
            return mStatusToolbar.getToolbar();
        }
        return null;
    }

    /**
     * 获取statusToolbar
     *
     * @return StatusToolbar
     */
    protected final StatusToolbar getStatusToolbar() {
        return mStatusToolbar;
    }

    /**
     * 提供状态View
     *
     * @return 状态View
     */
    private IStatusView provideStatusView() {
        IStatusView statusView = createStatusView();
        if (statusView != null) {
            statusView.setStatusViewProvider(this);
        }
        return statusView;
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
     * toolbar点击返回按钮（只有该Fragment中自己包含StatusToolbar才有效）
     */
    public void onNavigationBackPressed() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    /**
     * 可以由子类设置覆盖默认的view的Builder
     *
     * @param builder 内容View Builder
     * @param <T>     继承ContentViewBuilder
     */
    protected final <T extends ContentViewBuilder> void setContentViewBuilder(T builder) {
        this.mBuilder = builder;
    }

    /**
     * 由子类覆盖该方法，可改变{@link ContentViewBuilder}的属性
     */
    protected void onChangeContentView(ContentViewBuilder builder) {
    }


    /**
     * 创建状态View
     *
     * @return 状态View
     */
    protected IStatusView createStatusView() {
        return new StatusView(getActivity());
    }

    /**
     * 获取StatusToolbar的布局layout资源，注意根布局必须为{@link StatusToolbar}
     *
     * @return 资源id
     */
    protected int getStatusToolbarLayout() {
        return 0;
    }

    /**
     * 内容layout资源（必须覆盖该方法）
     *
     * @return layout资源id
     */
    @LayoutRes
    protected abstract int getLayout();
}
