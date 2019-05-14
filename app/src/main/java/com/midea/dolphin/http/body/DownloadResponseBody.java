package com.midea.dolphin.http.body;

import android.content.Context;
import android.text.TextUtils;


import com.midea.dolphin.http.callback.IProgressCallBack;
import com.midea.dolphin.http.exception.HttpException;
import com.midea.dolphin.http.utils.HttpLogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 下载请求体,主要用来回调进度和保存文件
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class DownloadResponseBody extends ResponseBody {

    private Context mContext;

    private String mFilePath;

    private String mFileName;

    private static final String APK_CONTENTTYPE = "application/vnd.android.package-archive";

    private static final String PNG_CONTENTTYPE = "image/png";

    private static final String JPG_CONTENTTYPE = "image/jpg";

    private static String file_suffix = "";

    private ResponseBody responseBody;

    private IProgressCallBack mCallBack;

    private BufferedSource bufferedSource;

    private CompositeDisposable mCompositeDisposable;

    public DownloadResponseBody(Context context, String filePath, String fileName, ResponseBody responseBody,
                                IProgressCallBack progressCallBack) {
        this.mFilePath = filePath;
        this.mFileName = fileName;
        this.mContext = context;
        this.responseBody = responseBody;
        this.mCallBack = progressCallBack;
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    /**
     * 处理数据
     *
     * @param source 数据源
     * @return 返回处理后的数据源
     */
    private Source source(Source source) {
        String downloadFilePath = createDownloadFile(mFilePath, mFileName, mContext);
        if (mCallBack != null) {
            Disposable disposable = Observable.just(contentLength())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long contentLength) {
                            HttpLogUtil.d("Download onStart....");
                            mCallBack.onStart();
                        }
                    });
            mCompositeDisposable.add(disposable);
        }
        return new DownloadingSource(source, downloadFilePath);

    }

    /**
     * 创建下载文件
     */
    private String createDownloadFile(String path, String name, Context context) {
        MediaType mediaType = contentType();
        if (!TextUtils.isEmpty(name) && mediaType != null) {
            String type = mediaType.toString();
            HttpLogUtil.d("contentType: " + type);
            if (!name.contains(".")) {
                if (type.equals(APK_CONTENTTYPE)) {
                    file_suffix = ".apk";
                } else if (type.equals(PNG_CONTENTTYPE)) {
                    file_suffix = ".png";
                } else if (type.equals(JPG_CONTENTTYPE)) {
                    file_suffix = ".jpg";
                } else {
                    file_suffix = "." + mediaType.subtype();
                }
                name = name + file_suffix;
            }
        } else {
            name = System.currentTimeMillis() + file_suffix;
        }
        if (path == null && context != null) {
            path = context.getExternalFilesDir(null) + File.separator + name;
        } else {
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            path = path + File.separator + name;
            path = path.replaceAll("//", "/");
        }
        HttpLogUtil.i("path:" + path);
        return path;
    }

    final class DownloadingSource extends ForwardingSource {

        private long mBytesWritten = 0;

        //最后一次刷新的时间
        private long mLastRefreshUiTime;

        private boolean hasErrors;

        private String mFilePath;

        private FileOutputStream mFileOutputStream;

        byte[] fileReader = new byte[1024 * 2];

        public DownloadingSource(Source delegate, String downloadFilePath) {
            super(delegate);
            mFilePath = downloadFilePath;
            try {
                mFileOutputStream = new FileOutputStream(new File(mFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public long read(@NonNull Buffer source, long byteCount) throws IOException {
            long bytesRead = 0;
            if (hasErrors) {
                source.skip(byteCount);
                return bytesRead;
            }
            try {
                bytesRead = super.read(source, byteCount);
            } catch (IOException e) {
                onException(e);
            }
            writeSourceToFile(mFileOutputStream, source);
            return bytesRead;
        }

        @Override
        public void close() {
            if (hasErrors) {
                return;
            }
            try {
                super.close();
            } catch (IOException e) {
                onException(e);
            }
        }

        private void writeSourceToFile(FileOutputStream outputStream, Buffer source) {
            try {
                InputStream inputStream = null;
                final long fileSize = contentLength();
                HttpLogUtil.d("Download file length: " + fileSize);
                try {
                    inputStream = source.inputStream();
                    while (true) {
                        int read = inputStream.read(fileReader);
                        if (read == -1) {
                            break;
                        }
                        outputStream.write(fileReader, 0, read);
                        mBytesWritten += read;
                        long curTime = System.currentTimeMillis();
                        //每50毫秒刷新一次数据,防止频繁更新进度
                        if (curTime - mLastRefreshUiTime >= 50 || mBytesWritten == fileSize) {
                            if (mCallBack != null) {
                                Disposable disposable = Observable.just(mBytesWritten)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<Long>() {
                                            @Override
                                            public void accept(Long fileSizeDownloaded) {
                                                mCallBack.onProgress(mFilePath,fileSizeDownloaded, fileSize);
                                            }
                                        });
                                mCompositeDisposable.add(disposable);
                            }
                            mLastRefreshUiTime = System.currentTimeMillis();
                            HttpLogUtil.i("Download process: " + mBytesWritten + " of " + fileSize);
                        }
                    }
                    if (mCallBack != null && mBytesWritten == fileSize) {
                        Disposable disposable = Observable.just(mFilePath).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<String>() {
                                    @Override
                                    public void accept(String path) {
                                        mCallBack.onComplete(path);
                                        mCompositeDisposable.dispose();
                                    }
                                });
                        HttpLogUtil.i("Download is success");
                    }
                } catch (IOException e) {
                    onException(e);
                } finally {
                    if(mBytesWritten == fileSize || hasErrors){
                        outputStream.flush();
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch (IOException e) {
                onException(e);
            }
        }

        private void onException(IOException e) {
            hasErrors = true;
            if (mCallBack != null) {
                Disposable disposable = Observable.just(new HttpException(e, HttpException.ERROR_UNKNOWN))
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<HttpException>() {
                            @Override
                            public void accept(HttpException e) {
                                HttpLogUtil.i("Download is fail");
                                mCallBack.onError(HttpException.handleException(e));
                                mCompositeDisposable.dispose();
                            }
                        });
            }
        }
    }
}
