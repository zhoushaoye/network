package com.midea.dolphin.http.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 头部参数
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class HttpHeaders implements Serializable {

    private LinkedHashMap<String, String> mHeadersMap;

    public HttpHeaders() {
        init();
    }

    private void init() {
        mHeadersMap = new LinkedHashMap<>();
    }

    public void put(String key, String value) {
        if (key != null && value != null) {
            mHeadersMap.remove(key);
            mHeadersMap.put(key, value);
        }
    }

    public void put(HttpHeaders headers) {
        if (headers != null) {
            if (headers.mHeadersMap != null && !headers.mHeadersMap.isEmpty()) {
                Set<Map.Entry<String, String>> set = headers.mHeadersMap.entrySet();
                for (Map.Entry<String, String> map : set) {
                    mHeadersMap.remove(map.getKey());
                    mHeadersMap.put(map.getKey(), map.getValue());
                }
            }

        }
    }

    public boolean isEmpty() {
        return mHeadersMap.isEmpty();
    }

    public String get(String key) {
        return mHeadersMap.get(key);
    }

    public String remove(String key) {
        return mHeadersMap.remove(key);
    }

    public void remove(HttpHeaders headers) {
        if (headers != null) {
            if (headers.mHeadersMap != null && !headers.mHeadersMap.isEmpty()) {
                Set<Map.Entry<String, String>> set = headers.mHeadersMap.entrySet();
                for (Map.Entry<String, String> map : set) {
                    mHeadersMap.remove(map.getKey());
                }
            }

        }
    }

    public void clear() {
        mHeadersMap.clear();
    }

    public int size() {
        return mHeadersMap.size();
    }

    public LinkedHashMap<String, String> getHeadersMap() {
        return mHeadersMap;
    }

    @Override
    public String toString() {
        return "HttpHeaders{" + "headersMap=" + mHeadersMap + '}';
    }
}