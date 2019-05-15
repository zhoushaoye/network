package com.midea.dolphin.base.baseactivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.midea.dolphin.base.utils.ActivityManager;
import com.midea.dolphin.base.view.IStatusView;
import com.midea.dolphin.base.view.StatusView;
import com.midea.dolphin.base.widget.IStatusViewProvider;
import com.midea.dolphin.base.mvp.IView;
import com.midea.dolphin.base.rx.RxLifecycleCompositor;
import com.midea.dolphin.base.utils.StatusBarUtil;
import com.midea.dolphin.base.view.ContentViewBuilder;
import com.midea.dolphin.base.widget.StatusToolbar;
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle;
import com.trello.rxlifecycle3.LifecycleProvider;
import com.trello.rxlifecycle3.LifecycleTransformer;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Lifecycle;
import io.reactivex.disposables.Disposable;

/**
 * 基础Activity，所有的Activity都集成与此Activity
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public abstract class BaseActivity extends AppCompatActivity implements IView, IStatusViewProvider {

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().pushActivity(this);
        int layoutRes = getLayout();
        if (layoutRes != 0) {
            setContentView(layoutRes);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        View contentView = LayoutInflater.from(this).inflate(layoutResID, null);
        setContentView(contentView);
    }

    @Override
    public void setContentView(View view) {
        View contentView = covertContentView(view);
        super.setContentView(contentView);
        initActionBar();
        onViewCreated(contentView);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        View contentView = covertContentView(view);
        super.setContentView(contentView, params);
        initActionBar();
        onViewCreated(contentView);
    }

    /**
     * 绑定statusBar+Toolbar+内容页
     *
     * @return 根布局View
     */
    private View covertContentView(View contentView) {
        this.contentView = contentView;
        mBuilder = new ContentViewBuilder(this);
        onChangeContentView(mBuilder);
        if (mBuilder.isHasHeaderBar()) {
            mStatusToolbar = mBuilder.getStatusToolbar();
            if (mStatusToolbar == null) {
                int layout = getStatusToolbarLayout();
                if (layout != 0) {
                    mStatusToolbar = (StatusToolbar) LayoutInflater.from(this).inflate(layout, null);
                    mBuilder.statusToolbar(mStatusToolbar);
                } else {
                    mStatusToolbar = new StatusToolbar(this);
                }
            }
        } else {
            // 如果没有status+toolbar，则全部沉浸式展示Activity
            StatusBarUtil.fillStatusBar(this);
        }
        if (mBuilder.isHasLoadStatus()) {
            mStatusView = mBuilder.getStatusView();
            if (mStatusView == null) {
                mStatusView = new StatusView(this);
            }
            mStatusView.setStatusViewProvider(this);
        }
        mBuilder.statusView(mStatusView);
        return mBuilder.build(contentView);
    }


    /**
     * 初始化ActionBar
     */
    private void initActionBar() {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            setSupportActionBar(getToolbar());
            ActionBar actionBar = this.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    /**
     * 隐藏返回按钮
     */
    public void hideNavigationBack() {
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    protected IStatusView getStatusView() {
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
     * 从Activity生命周期绑定的Disposable中移除Disposable
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

    /**
     * View创建成功回调
     *
     * @param contentView 根布局View
     */
    protected abstract void onViewCreated(View contentView);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().popActivity(this);
    }
}
