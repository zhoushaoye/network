package com.midea.dolphin.http.cookie;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Cookie管理器,主要是对请求和结果进行Cookie管理
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class CookieManger implements CookieJar {

    protected PersistentCookieStore mCookieStore;

    public CookieManger(Context context) {
        if (mCookieStore == null) {
            mCookieStore = new PersistentCookieStore(context);
        }
    }

    public void addCookies(List<Cookie> cookies) {
        mCookieStore.addCookies(cookies);
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookies != null && cookies.size() > 0) {
            for (Cookie item : cookies) {
                mCookieStore.addCookie(url, item);
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = mCookieStore.get(url);
        return cookies != null ? cookies : new ArrayList<Cookie>();
    }

    public void removeCookie(HttpUrl url, Cookie cookie) {
        mCookieStore.remove(url, cookie);
    }

    public void removeAllCookie() {
        mCookieStore.removeAll();
    }

}
