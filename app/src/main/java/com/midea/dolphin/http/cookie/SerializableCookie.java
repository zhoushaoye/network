package com.midea.dolphin.http.cookie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import okhttp3.Cookie;

/**
 * 对cookie进行序列化操作,实现参考com.loopj.android.http.SerializableCookie
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class SerializableCookie implements Serializable {

    private static final long serialVersionUID = 6374381828722046732L;
    private transient final Cookie mCookie;
    private transient Cookie mClientCookies;

    public SerializableCookie(Cookie cookies) {
        this.mCookie = cookies;
    }

    public Cookie getCookie() {
        Cookie bestCookies = mCookie;
        if (mClientCookies != null) {
            bestCookies = mClientCookies;
        }
        return bestCookies;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(mCookie.name());
        out.writeObject(mCookie.value());
        out.writeLong(mCookie.expiresAt());
        out.writeObject(mCookie.domain());
        out.writeObject(mCookie.path());
        out.writeBoolean(mCookie.secure());
        out.writeBoolean(mCookie.httpOnly());
        out.writeBoolean(mCookie.hostOnly());
        out.writeBoolean(mCookie.persistent());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        String name = (String) in.readObject();
        String value = (String) in.readObject();
        long expiresAt = in.readLong();
        String domain = (String) in.readObject();
        String path = (String) in.readObject();
        boolean secure = in.readBoolean();
        boolean httpOnly = in.readBoolean();
        boolean hostOnly = in.readBoolean();
        Cookie.Builder builder = new Cookie.Builder();
        builder = builder.name(name);
        builder = builder.value(value);
        builder = builder.expiresAt(expiresAt);
        builder = hostOnly ? builder.hostOnlyDomain(domain) : builder.domain(domain);
        builder = builder.path(path);
        builder = secure ? builder.secure() : builder;
        builder = httpOnly ? builder.httpOnly() : builder;
        mClientCookies =builder.build();
    }
}