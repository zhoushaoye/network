package com.midea.dolphin.http.callback;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import io.reactivex.disposables.Disposable;

/**
 * 带有Loading的回调
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public abstract class ProgressHttpCallBack<T> extends HttpCallBack<T> {

    private Dialog mDialog;

    public ProgressHttpCallBack(Context context) {
        String title = "Loading...";
        init(context, title, true);
    }

    public ProgressHttpCallBack(Context context, String title) {
        init(context, title, true);
    }

    public ProgressHttpCallBack(Context context, String title, boolean isCancel) {
        init(context, title, isCancel);
    }

    public ProgressHttpCallBack(Dialog dialog) {
        mDialog = dialog;
        init(null, "", false);
    }

    private void init(Context context, String title, boolean isCancel) {
        if (mDialog == null) {
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(title);
            progressDialog.setCancelable(isCancel);
            mDialog = progressDialog;
        }
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (disposed != null && !disposed.isDisposed()) {
                    disposed.dispose();
                }
            }
        });
    }

    private void showProgress() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }
    }

    private void dismissProgress() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onStart() {
        showProgress();
    }

    @Override
    public void onComplete() {
        dismissProgress();
    }

    private Disposable disposed;

    public void subscription(Disposable disposed) {
        this.disposed = disposed;
    }
}
