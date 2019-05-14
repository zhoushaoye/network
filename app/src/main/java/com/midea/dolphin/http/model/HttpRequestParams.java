package com.midea.dolphin.http.model;

import android.webkit.MimeTypeMap;


import com.midea.dolphin.http.utils.HttpLogUtil;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;

/**
 * 请求参数,文件
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class HttpRequestParams implements Serializable {

    private LinkedHashMap<String, String> mRequestParams;

    private LinkedHashMap<String, List<FileWrapper>> mFileParams;

    public HttpRequestParams() {
        init();
    }

    private void init() {
        mRequestParams = new LinkedHashMap<>();
        mFileParams = new LinkedHashMap<>();
    }

    public void put(HttpRequestParams params) {
        if (params != null) {
            if (params.mRequestParams != null && !params.mRequestParams.isEmpty()) {
                mRequestParams.putAll(params.mRequestParams);
            }

            if (params.mFileParams != null && !params.mFileParams.isEmpty()) {
                mFileParams.putAll(params.mFileParams);
            }
        }
    }

    public void put(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> stringStringEntry : params.entrySet()) {
            String value = stringStringEntry.getValue();
            if(null == value){
                value = "";
            }
            put(stringStringEntry.getKey(), value);
        }
    }

    public void put(String key, String value) {
        if(null == value){
            value = "";
        }
        mRequestParams.put(key, value);
    }

    public void put(String key, int value) {
        mRequestParams.put(key, String.valueOf(value));
    }

    public void put(String key, float value) {
        mRequestParams.put(key, String.valueOf(value));
    }
    public void put(String key, long value) {
        mRequestParams.put(key, String.valueOf(value));
    }
    public void put(String key, boolean value) {
        mRequestParams.put(key, String.valueOf(value));
    }

    public <T extends File> void putFileParam(String key, T file) {
        putFileParam(key, file, file.getName());
    }

    public <T extends File> void putFileParam(String key, T file, String fileName) {
        putFileParam(key, file, fileName, getFileMimeType(fileName));
    }

    public <T extends InputStream> void putFileParam(String key, T file, String fileName) {
        putFileParam(key, file, fileName, getFileMimeType(fileName));
    }

    public void putFileParam(String key, byte[] bytes, String fileName) {
        putFileParam(key, bytes, fileName, getFileMimeType(fileName));
    }

    public void putFileParam(String key, FileWrapper fileWrapper) {
        if (key != null && fileWrapper != null) {
            putFileParam(key, fileWrapper.mFile, fileWrapper.mFileName, fileWrapper.mContentType);
        }
    }

    public <T> void putFileParam(String key, T countent, String fileName, MediaType contentType) {
        if (key != null) {
            List<FileWrapper> fileWrappers = mFileParams.get(key);
            if (fileWrappers == null) {
                fileWrappers = new ArrayList<>();
                mFileParams.put(key, fileWrappers);
            }
            fileWrappers.add(new FileWrapper(countent, fileName, contentType));
        }
    }

    public <T extends File> void putFileParams(String key, List<T> files) {
        if (key != null && files != null && !files.isEmpty()) {
            for (File file : files) {
                putFileParam(key, file);
            }
        }
    }

    public void putFileWrapperParams(String key, List<FileWrapper> fileWrappers) {
        if (key != null && fileWrappers != null && !fileWrappers.isEmpty()) {
            for (FileWrapper fileWrapper : fileWrappers) {
                putFileParam(key, fileWrapper);
            }
        }
    }

    public void removeRequestParam(String key) {
        mRequestParams.remove(key);
    }


    public void removeFileParam(String key) {
        mFileParams.remove(key);
    }

    public void remove(String key) {
        removeRequestParam(key);
        removeFileParam(key);
    }

    public void remove(HttpRequestParams params) {
        if (params != null) {
            if (params.mRequestParams != null && !params.mRequestParams.isEmpty()) {
                mRequestParams.remove(params.mRequestParams);
            }

            if (params.mFileParams != null && !params.mFileParams.isEmpty()) {
                mFileParams.remove(params.mFileParams);
            }
        }
    }

    public void clear() {
        mRequestParams.clear();
        mFileParams.clear();
    }

    private MediaType getFileMimeType(String path) {
        String extensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(path);
        String contentType = "application/octet-stream";
        if (extensionFromUrl != null) {
            contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extensionFromUrl);
        }
        HttpLogUtil.d("File contentType: " + contentType);
        return MediaType.parse(contentType);
    }

    public LinkedHashMap<String, String> getRequestParams() {
        return mRequestParams;
    }

    public LinkedHashMap<String, List<FileWrapper>> getFileParams() {
        return mFileParams;
    }


    public static class FileWrapper<T> {

        private T mFile;

        private String mFileName;

        private MediaType mContentType;

        private long mFileSize;

        public FileWrapper(T file, String fileName, MediaType contentType) {
            this.mFile = file;
            this.mFileName = fileName;
            this.mContentType = contentType;
            if (file instanceof File) {
                this.mFileSize = ((File) file).length();
            } else if (file instanceof byte[]) {
                this.mFileSize = ((byte[]) file).length;
            }
        }

        public T getFile() {
            return mFile;
        }

        public String getFileName() {
            return mFileName;
        }

        public MediaType getContentType() {
            return mContentType;
        }

        public long getFileSize() {
            return mFileSize;
        }

    }


}