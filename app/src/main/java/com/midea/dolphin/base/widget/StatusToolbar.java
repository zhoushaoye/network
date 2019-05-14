package com.midea.dolphin.base.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.midea.dolphin.R;
import com.midea.dolphin.base.util.StatusBarUtil;


/**
 * 状态栏 + toolBar作为整体View
 * 可在layout文件中，包含一个toolbar，如果未包含，则会在加载完View之后，根据参数{@link #isShowToolbar}判断是否动态加载Toolbar
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public class StatusToolbar extends LinearLayout {

    private int[] background = new int[]{
            R.attr.colorPrimary,
            R.attr.colorPrimaryDark
    };

    private Activity mActivity;

    private Toolbar mToolbar;

    private View mStatusBar;

    private int toolbarColor;

    private boolean isDarkStatusBar;

    private boolean isSetStatusBar;

    private boolean isShowToolbar;

    private final PassThroughHierarchyChangeListener mPassThroughListener;

    public StatusToolbar(Context context) {
        this(context, null);
    }

    public StatusToolbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
        mPassThroughListener = new PassThroughHierarchyChangeListener();
        super.setOnHierarchyChangeListener(mPassThroughListener);
    }

    private void initView(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StatusToolbar);
        isShowToolbar = a.getBoolean(R.styleable.StatusToolbar_show_toolbar, true);
        mToolbar = findToolbar(this);

        TypedArray array = context.getTheme().obtainStyledAttributes(background);
        toolbarColor = array.getColor(0, Integer.MIN_VALUE);

        int statusHeight = getStatusBarHeight();
        if (statusHeight > 0) {
            // 添加状态栏空白View
            mStatusBar = new View(context);
            addView(mStatusBar, 0, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusHeight));
            fillStatusBar();
            // 设置状态栏颜色
            int statusColor = a.getColor(R.styleable.StatusToolbar_status_background, Integer.MIN_VALUE);
            if (statusColor == Integer.MIN_VALUE) {
                statusColor = array.getColor(1, Integer.MIN_VALUE);
            }
            if (statusColor != Integer.MIN_VALUE) {
                setStatusBarColor(statusColor);
            }
        }
        array.recycle();
        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        addToolbar();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addToolbar();
    }

    /**
     * 设置是否显示Toolbar
     *
     * @param showToolbar 是否显示Toolbar
     */
    public void showToolbar(boolean showToolbar) {
        isShowToolbar = showToolbar;
        if (!isShowToolbar) {
            if (mToolbar != null) {
                removeView(mToolbar);
            }
        } else {
            addToolbar();
        }
    }

    /**
     * 根据参数动态添加Toolbar
     */
    private void addToolbar() {
        if (mToolbar == null && isShowToolbar) {
            Toolbar toolbar = new Toolbar(getContext());
            if (toolbarColor != Integer.MIN_VALUE) {
                toolbar.setBackgroundColor(toolbarColor);
            }
            setToolbar(toolbar);
        }
    }

    /**
     * 设置状态栏颜色
     *
     * @param color 颜色值
     */
    public void setStatusBarColor(@ColorInt int color) {
        if (mStatusBar != null) {
            mStatusBar.setBackgroundColor(color);
            boolean isDark = isColorDark(color);
            setStatusBarForeground(isDark);
        }
    }

    /**
     * 绑定toolbar
     *
     * @param toolbar toolbar
     */
    public void setToolbar(Toolbar toolbar) {
        if (toolbar == null) {
            return;
        }
        if (mToolbar != null) {
            removeView(mToolbar);
        }
        mToolbar = toolbar;
        if (toolbar.getParent() instanceof ViewGroup) {
            ((ViewGroup) toolbar.getParent()).removeView(toolbar);
        }
        // 第一个为statusBar
        addView(toolbar);
    }

    /**
     * 获取toolbar
     *
     * @return Toolbar
     */
    public Toolbar getToolbar() {
        return mToolbar;
    }

    /**
     * 填充系统的状态栏
     */
    private void fillStatusBar() {
        if (mStatusBar == null) {
            return;
        }
        StatusBarUtil.fillStatusBar(mActivity);
    }

    /**
     * 从布局中找到Toolbar
     *
     * @param view 该布局包含的子View
     * @return Toolbar
     */
    private Toolbar findToolbar(View view) {
        if (view instanceof Toolbar) {
            return (Toolbar) view;
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                Toolbar toolbar = findToolbar(child);
                if (toolbar != null) {
                    return toolbar;
                }
            }
        }
        return null;
    }

    /**
     * 获取状态栏高度
     */
    private int getStatusBarHeight() {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    @Override
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        mPassThroughListener.mOnHierarchyChangeListener = listener;
    }

    /**
     * 判断是否为深色
     *
     * @param color 颜色纸
     * @return 是否为深色
     */
    public static boolean isColorDark(@ColorInt int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }


    /**
     * 设置Android状态栏前景色(字体和图标)
     *
     * @param isDark 状态栏字体是否为深色
     */
    @SuppressLint("PrivateApi")
    public void setStatusBarForeground(boolean isDark) {
        if (mActivity == null) {
            return;
        }
        // 如果跟上次设置的一致，则不进行设置
        if (isSetStatusBar && isDark == isDarkStatusBar) {
            return;
        }
        isDarkStatusBar = isDark;
        isSetStatusBar = true;
        StatusBarUtil.setStatusBarForeground(mActivity, isDark);
    }

    /**
     * 监听toolbar的添加移除事件
     */
    private class PassThroughHierarchyChangeListener implements OnHierarchyChangeListener {

        private OnHierarchyChangeListener mOnHierarchyChangeListener;

        @Override
        public void onChildViewAdded(View parent, View child) {
            mToolbar = findToolbar(child);
            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
            Toolbar toolbar = findToolbar(child);
            if (toolbar != null && toolbar == mToolbar) {
                mToolbar = null;
            }
            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }
}
