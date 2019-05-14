
package com.midea.dolphin.http.body;





import com.midea.dolphin.http.callback.IProgressCallBack;
import com.midea.dolphin.http.exception.HttpException;
import com.midea.dolphin.http.utils.HttpLogUtil;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * <p>描述：上传请求体</p>
 * 1.具有上传进度回调通知功能<br>
 * 2.防止频繁回调，上层无用的刷新<br>
 */
public class UploadRequestBody extends RequestBody {

    private RequestBody mRequestBody;

    private IProgressCallBack mUploadProgressCallBack;

    private String mPath;

    private CompositeDisposable mCompositeDisposable;

    public UploadRequestBody(RequestBody requestBody, String path, IProgressCallBack progressCallBack) {
        this.mRequestBody = requestBody;
        this.mUploadProgressCallBack = progressCallBack;
        this.mPath = path;
        mCompositeDisposable = new CompositeDisposable();
        if (mUploadProgressCallBack != null) {
            Disposable disposable = Observable.just(1L)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long contentLength) {
                            HttpLogUtil.d("Upload onStart....");
                            mUploadProgressCallBack.onStart();
                        }
                    });
            mCompositeDisposable.add(disposable);
        }
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();

    }

    /**
     * 重写调用实际的响应体的contentLength
     */
    @Override
    public long contentLength() {
        try {
            return mRequestBody.contentLength();
        } catch (IOException e) {
            HttpLogUtil.e(e.getMessage());
            return -1;
        }
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink bufferedSink;

        CountingSink countingSink = new CountingSink(sink);
        bufferedSink = Okio.buffer(countingSink);

        mRequestBody.writeTo(bufferedSink);

        bufferedSink.flush();
    }


    final class CountingSink extends ForwardingSink {

        private long mBytesWritten = 0;

        private long mContentLength = 0;

        private long mLastRefreshUiTime;

        private boolean hasErrors;

        public CountingSink(Sink sink) {
            super(sink);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            if (hasErrors) {
                source.skip(byteCount);
                return;
            }
            try {
                super.write(source, byteCount);
            } catch (IOException e) {
                hasErrors = true;
                onException(e);
            }
            if (mContentLength <= 0) {
                mContentLength = contentLength();
            }

            //增加当前写入的字节数
            mBytesWritten += byteCount;

            long curTime = System.currentTimeMillis();
            if (curTime - mLastRefreshUiTime >= 20 || mBytesWritten == mContentLength) {
                if (mUploadProgressCallBack != null) {
                    Disposable disposable = Observable.just(mBytesWritten)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Long>() {
                                @Override
                                public void accept(Long bytesWritten) {
                                    mUploadProgressCallBack.onProgress(mPath,bytesWritten, mContentLength);
                                }
                            });
                    mCompositeDisposable.add(disposable);
                }
                mLastRefreshUiTime = System.currentTimeMillis();
                HttpLogUtil.i("writBytes = " + mBytesWritten + " ,totalBytes = " + mContentLength);
            }

            if (mBytesWritten == mContentLength) {
                if (mUploadProgressCallBack != null) {
                    Disposable disposable = Observable.just(mPath)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<String>() {
                                @Override
                                public void accept(@NonNull String path) {
                                    HttpLogUtil.d("Upload onComplete...."+path);
                                    mUploadProgressCallBack.onComplete(path);
                                    mCompositeDisposable.dispose();
                                }
                            });
                    mCompositeDisposable.add(disposable);
                }
            }
        }

        @Override
        public void flush() {
            if (hasErrors) {
                return;
            }
            try {
                super.flush();
            } catch (IOException e) {
                hasErrors = true;
                onException(e);
            }
        }

        @Override
        public void close() {
            if (hasErrors) {
                return;
            }
            try {
                super.close();
            } catch (IOException e) {
                hasErrors = true;
                onException(e);
            }
        }

        private void onException(IOException e) {
            if (mUploadProgressCallBack != null) {
                Disposable disposable = Observable.just(new HttpException(e, HttpException.ERROR_UNKNOWN))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<HttpException>() {
                            @Override
                            public void accept(@NonNull HttpException e) {
                                HttpLogUtil.d("Upload HttpException....");
                                mUploadProgressCallBack.onError(HttpException.handleException(e));
                                mCompositeDisposable.dispose();
                            }
                        });
                mCompositeDisposable.add(disposable);
            }
        }
    }

}
