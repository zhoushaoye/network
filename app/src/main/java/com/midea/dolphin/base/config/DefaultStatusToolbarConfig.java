package com.midea.dolphin.base.config;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.midea.dolphin.R;
import com.midea.dolphin.base.widget.StatusToolbar;

/**
 *  默认的statustoolbar配置
 *  具体不同时可以继承此类做修改
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public class DefaultStatusToolbarConfig implements IStatusToolbarConfig {

    private Activity mActivity;

    /**
     * toolbar 所在界面的整个view
     */
    private View mContentView;

    /**
     * 需要配置管理的 StatusToolbar
     */
    protected StatusToolbar mStatusToolbar;

    protected ImageView mImageView;
    protected TextView mTitleView;

    protected TextView mRightTextView;

    @Override
    public void init(Activity activity, View contentView, StatusToolbar statusToolbar) {
        mActivity = activity;
        mContentView = contentView;
        mStatusToolbar = statusToolbar;
        mImageView = mStatusToolbar.findViewById(R.id.left_imageView);
        mTitleView = statusToolbar.findViewById(R.id.title);
        mRightTextView = statusToolbar.findViewById(R.id.right_textview);
    }

    @Override
    public void config() {
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.finish();
            }
        });
    }

    @Override
    public StatusToolbar getStatusToolbar() {
        return mStatusToolbar;
    }

}


