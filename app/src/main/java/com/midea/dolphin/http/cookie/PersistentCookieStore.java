package com.midea.dolphin.http.cookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


import com.midea.dolphin.http.utils.HttpLogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Cookie;
import okhttp3.HttpUrl;


/**
 * 保存Cookie,进行了简单的decode,实现参考com.loopj.android.http.PersistentCookieStore
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class PersistentCookieStore {

    private static final String COOKIE_PREFS = "CookiePrefsFile";

    private final Map<String, ConcurrentHashMap<String, Cookie>> mCookies;

    private static final String COOKIE_NAME_PREFIX = "cookie_";

    private final SharedPreferences mCookiePrefs;


    public PersistentCookieStore(Context context) {
        mCookiePrefs = context.getSharedPreferences(COOKIE_PREFS, 0);
        mCookies = new HashMap<>();
        Map<String, ?> prefsMap = mCookiePrefs.getAll();
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            String[] cookieNames = TextUtils.split((String) entry.getValue(), ",");
            for (String name : cookieNames) {
                String encodedCookie = mCookiePrefs.getString(COOKIE_NAME_PREFIX + name, null);
                if (encodedCookie != null) {
                    Cookie decodedCookie = decodeCookie(encodedCookie);
                    if (decodedCookie != null) {
                        if (!mCookies.containsKey(entry.getKey())) {
                            mCookies.put(entry.getKey(), new ConcurrentHashMap<String, Cookie>());
                        }
                        mCookies.get(entry.getKey()).put(name, decodedCookie);
                    }
                }
            }
        }
    }

    protected String getCookieName(Cookie cookie) {
        return cookie.name() + cookie.domain();
    }

    public void addCookie(HttpUrl url, Cookie cookie) {
        String name = getCookieName(cookie);
        String host = url.host();
        if (!mCookies.containsKey(host)) {
            mCookies.put(host, new ConcurrentHashMap<String, Cookie>());
        }
        ConcurrentHashMap<String, Cookie> cookies = mCookies.get(host);
        if (mCookies.containsKey(host)) {
            if(cookies.containsKey(name)){
                cookies.remove(name);
            }
        }
        cookies.put(name, cookie);
        SharedPreferences.Editor prefsWriter = mCookiePrefs.edit();
        if (cookie.persistent()) {
            prefsWriter.putString(host, TextUtils.join(",", cookies.keySet()));
            prefsWriter.putString(COOKIE_NAME_PREFIX + name, encodeCookie(new SerializableCookie(cookie)));
        } else {
            prefsWriter.remove(host);
            prefsWriter.remove(name);
        }
        prefsWriter.apply();
    }

    public void addCookies(List<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            String domain = cookie.domain();
            ConcurrentHashMap<String, Cookie> domainCookies = this.mCookies.get(domain);
            if (domainCookies == null) {
                domainCookies = new ConcurrentHashMap<>();
                this.mCookies.put(domain, domainCookies);
            }
        }
    }

    public List<Cookie> get(HttpUrl url) {
        ArrayList<Cookie> cookieArrayList = new ArrayList<>();
        if (mCookies.containsKey(url.host())) {
            cookieArrayList.addAll(mCookies.get(url.host()).values());
        }
        return cookieArrayList;
    }

    public boolean removeAll() {
        SharedPreferences.Editor prefsWriter = mCookiePrefs.edit();
        prefsWriter.clear();
        prefsWriter.apply();
        mCookies.clear();
        return true;
    }

    public boolean remove(HttpUrl url, Cookie cookie) {
        String name = getCookieName(cookie);

        if (mCookies.containsKey(url.host()) && mCookies.get(url.host()).containsKey(name)) {
            mCookies.get(url.host()).remove(name);

            SharedPreferences.Editor prefsWriter = mCookiePrefs.edit();
            if (mCookiePrefs.contains(name)) {
                prefsWriter.remove(COOKIE_NAME_PREFIX + name);
            }
            prefsWriter.putString(url.host(), TextUtils.join(",", mCookies.get(url.host()).keySet()));
            prefsWriter.apply();

            return true;
        } else {
            return false;
        }
    }

    public List<Cookie> getCookies() {
        ArrayList<Cookie> cookies = new ArrayList<>();
        for (String key : mCookies.keySet()) {
            cookies.addAll(mCookies.get(key).values());
        }
        return cookies;
    }

    protected String encodeCookie(SerializableCookie cookie) {
        if (cookie == null) {
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookie);
        } catch (IOException e) {
            HttpLogUtil.d("IOException in encodeCookie" + e.getMessage());
            return null;
        }

        return byteArrayToHexString(os.toByteArray());
    }

    protected Cookie decodeCookie(String cookieString) {
        byte[] bytes = hexStringToByteArray(cookieString);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Cookie cookie = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object readObject = objectInputStream.readObject();
            if(readObject instanceof SerializableCookie){
                cookie = ((SerializableCookie) readObject).getCookie();
            }
        } catch (IOException e) {
            HttpLogUtil.d("IOException in decodeCookie" + e.getMessage());
        } catch (ClassNotFoundException e) {
            HttpLogUtil.d("ClassNotFoundException in decodeCookie" + e.getMessage());
        }

        return cookie;
    }

    protected String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    protected byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
                    .digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}