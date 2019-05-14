package com.midea.dolphin.base.baseactivity;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.midea.dolphin.R;
import com.midea.dolphin.base.config.DefaultStatusToolbarConfig;
import com.midea.dolphin.base.widget.DolphinStatusViewProvider;
import com.midea.dolphin.base.config.IStatusToolbarConfig;
import com.midea.dolphin.base.config.PageConfig;
import com.midea.dolphin.base.widget.StatusViewProvider;
import com.midea.dolphin.base.utils.KeyboardUtil;
import com.midea.dolphin.base.view.ContentViewBuilder;
import com.midea.dolphin.base.widget.StatusToolbar;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 *  基础Activity
 * <p>
 * 提供的功能
 * 1. 工具栏
 * 2. 状态栏
 * 3. 页面加载状态
 * 4. 加载进度弹窗
 * 5. 可配置工具栏，状态栏，页面加载。
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public abstract class BaseAppActivity extends BaseActivity {

    private Unbinder unbinder;

    /**
     * 基础类activity 的配置
     */
    private PageConfig mPageConfig;

    /**
     * 获取自定义的工具栏
     */
    private StatusToolbar mStatusToolbar;

    /**
     * 正在加载中的进度框
     */
    private ProgressDialog mProgressDialog;

    /**
     * 提供页面各种状态
     */
    private StatusViewProvider mStatusViewProvider;

    /**
     * 更新页面的配置，根据页面不同做不同的配置，有些需要改变的
     * 请重写此类
     *
     * @param pageConfig
     */
    public void updatePageConfig(PageConfig pageConfig) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mPageConfig = new PageConfig();
        updatePageConfig(mPageConfig);
        // 如果有toolbar 并且 updatePageConfig没有有配置statusToolbarConfig ，则使用默认
        if (mPageConfig.isToolbar() && mPageConfig.statusToolbarConfigIsNull()) {
            mPageConfig.setStatusToolbarConfig(new DefaultStatusToolbarConfig());
        }
        // 如果有页面状态。则创建provider 此代码必须在 super.onCreate(savedInstanceState); 之前调用创建
        // 因为super.onCreate(savedInstanceState); 里面有用到provider
        if (mPageConfig.isPageStatusView()) {
            mStatusViewProvider = new DolphinStatusViewProvider();
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    @CallSuper
    protected void onViewCreated(View contentView) {
        unbinder = ButterKnife.bind(this);
        // 关闭系统的toolbar使用自己的
        if (mPageConfig.isToolbar()) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(false);
            }

            mStatusToolbar = getStatusToolbar();
            mPageConfig.getStatusToolbarConfig().init(this, contentView, mStatusToolbar);
            mPageConfig.getStatusToolbarConfig().config();
            changeStatusToolbarConfig(mPageConfig.getStatusToolbarConfig());
        }

        initView(contentView);
    }

    /**
     * 用于子类初始化view
     *
     * @param contentView
     */
    protected abstract void initView(View contentView);

    /**
     * 此方法允许子类获取IStatusToolbarConfig 进行操作。
     * 正常情况下不用继承此类修改，特殊情况 一定需要在子类中获取StatusToolbar 时才继承此方法
     *
     * @param statusToolbarConfig
     */
    protected void changeStatusToolbarConfig(IStatusToolbarConfig statusToolbarConfig) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
        dismissProgressDialog();
    }

    @Override
    protected void onChangeContentView(ContentViewBuilder builder) {
        builder.hasToolbar(mPageConfig.isToolbar());
    }

    @Override
    protected int getStatusToolbarLayout() {
        return R.layout.basetoolbar_layout;
    }

    /**
     * 如果提供数据加载中时的页面显示，请复写此方法
     *
     * @param context
     * @param container
     * @return
     */
    @Override
    public View getLoadingView(Context context, ViewGroup container) {
        return mStatusViewProvider.getLoadingView(context, container);
    }

    /**
     * 如果提供数据为空时的页面显示，请复写此方法
     *
     * @param context
     * @param container
     * @return
     */
    @Override
    public View getEmptyView(Context context, ViewGroup container) {
        return mStatusViewProvider.getEmptyView(context, container);
    }

    /**
     * 如果提供数据加载错误的页面显示，请复写此方法
     *
     * @param context
     * @param container
     * @return
     */
    @Override
    public View getErrorView(Context context, ViewGroup container) {
        return mStatusViewProvider.getErrorView(context, container);
    }

    /**
     * 提供页面的加载进度框
     */
    public void showProgressDialog() {
        // 具体要根据UI提供的图，
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
        }
        mProgressDialog.show();
    }

    /**
     * 销毁进度框
     */
    public void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isKeyboardAutoHide()) {
            KeyboardUtil.dispatchTouchEvent(this, event);
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean isKeyboardAutoHide() {
        return true;
    }

}
