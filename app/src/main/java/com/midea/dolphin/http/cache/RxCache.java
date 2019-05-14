package com.midea.dolphin.http.cache;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;



import com.midea.dolphin.http.cache.converter.IDiskConverter;
import com.midea.dolphin.http.cache.converter.SerializableDiskConverter;
import com.midea.dolphin.http.cache.core.CacheCore;
import com.midea.dolphin.http.cache.core.LruDiskCache;
import com.midea.dolphin.http.cache.model.CacheMode;
import com.midea.dolphin.http.cache.model.CacheResult;
import com.midea.dolphin.http.cache.stategy.CacheAndRemoteDistinctStrategy;
import com.midea.dolphin.http.cache.stategy.CacheAndRemoteStrategy;
import com.midea.dolphin.http.cache.stategy.FirstCacheStategy;
import com.midea.dolphin.http.cache.stategy.FirstRemoteStrategy;
import com.midea.dolphin.http.cache.stategy.IStrategy;
import com.midea.dolphin.http.cache.stategy.NoStrategy;
import com.midea.dolphin.http.cache.stategy.OnlyCacheStrategy;
import com.midea.dolphin.http.cache.stategy.OnlyRemoteStrategy;
import com.midea.dolphin.http.utils.HttpLogUtil;
import com.midea.dolphin.http.utils.HttpUtil;


import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.exceptions.Exceptions;


/**
 * <p>描述：缓存统一入口类</p>
 * <p>
 * <p>主要实现技术：Rxjava+DiskLruCache(jakewharton大神开源的LRU库)</p>
 * <p>
 * <p>
 * 主要功能：<br>
 * 1.可以独立使用，单独用RxCache来存储数据<br>
 * 2.采用transformer与网络请求结合，可以实现网络缓存功能,本地硬缓存<br>
 * 3.可以保存缓存 （异步）<br>
 * 4.可以读取缓存（异步）<br>
 * 5.可以判断缓存是否存在<br>
 * 6.根据key删除缓存<br>
 * 7.清空缓存（异步）<br>
 * 8.缓存Key会自动进行MD5加密<br>
 * 9.其它参数设置：缓存磁盘大小、缓存key、缓存时间、缓存存储的转换器、缓存目录、缓存Version<br>
 * <p>
 * <p>
 * 使用说明：<br>
 * RxCache rxCache = new RxCache.Builder(this)<br>
 * .appVersion(1)//不设置，默认为1</br>
 * .diskDir(new File(getCacheDir().getPath() + File.separator + "data-cache"))//不设置,默认使用缓存路径<br>
 * .diskConverter(new SerializableDiskConverter())<br>
 * .diskMax(20*1024*1024)//不设置,默为认50MB<br>
 * .build();</br>
 * </P>
 */
public final class RxCache {

    private final Context mContext;

    //缓存的核心管理类
    private final CacheCore mCacheCore;

    //缓存的key
    private final String mCacheKey;

    //缓存的时间 单位:秒
    private final long mCacheTime;

    //缓存的转换器
    private final IDiskConverter mDiskConverter;

    //缓存的磁盘目录，默认是缓存目录
    private final File mDiskDir;

    //缓存的版本
    private final int mAppVersion;

    //缓存的磁盘大小
    private final long mDiskMaxSize;

    public RxCache() {
        this(new Builder());
    }

    private RxCache(Builder builder) {
        this.mContext = builder.context;
        this.mCacheKey = builder.cachekey;
        this.mCacheTime = builder.cacheTime;
        this.mDiskDir = builder.diskDir;
        this.mAppVersion = builder.appVersion;
        this.mDiskMaxSize = builder.diskMaxSize;
        this.mDiskConverter = builder.diskConverter;
        mCacheCore = new CacheCore(new LruDiskCache(mDiskConverter, mDiskDir, mAppVersion, mDiskMaxSize));
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * 缓存transformer
     *
     * @param cacheMode 缓存类型
     * @param type      缓存clazz
     */
    public <T> ObservableTransformer<T, CacheResult<T>> transformer(CacheMode cacheMode, final Type type) {
        final IStrategy strategy = loadStrategy(cacheMode);//获取缓存策略
        return new ObservableTransformer<T, CacheResult<T>>() {
            @Override
            public ObservableSource<CacheResult<T>> apply(@NonNull Observable<T> upstream) {
                HttpLogUtil.i("cackeKey=" + RxCache.this.mCacheKey);
                Type tempType = type;
                if (type instanceof ParameterizedType) {//自定义ApiResult
                    Class<T> cls = (Class) ((ParameterizedType) type).getRawType();
                    if (CacheResult.class.isAssignableFrom(cls)) {
                        tempType = HttpUtil.getParameterizedType(type, 0);
                    }
                }
                return strategy
                        .execute(RxCache.this, RxCache.this.mCacheKey, RxCache.this.mCacheTime, upstream, tempType);
            }
        };
    }

    private static abstract class SimpleSubscribe<T> implements ObservableOnSubscribe<T> {

        @Override
        public void subscribe(@NonNull ObservableEmitter<T> subscriber) throws Exception {
            try {
                T data = execute();
                if (!subscriber.isDisposed()) {
                    subscriber.onNext(data);
                }
            } catch (Throwable e) {
                HttpLogUtil.e(e.getMessage());
                if (!subscriber.isDisposed()) {
                    subscriber.onError(e);
                }
                Exceptions.throwIfFatal(e);
                //RxJavaPlugins.onError(e);
                return;
            }

            if (!subscriber.isDisposed()) {
                subscriber.onComplete();
            }
        }

        abstract T execute() throws Throwable;
    }

    /**
     * 获取缓存
     *
     * @param type 保存的类型
     * @param key  缓存key
     */
    public <T> Observable<T> load(final Type type, final String key) {
        return load(type, key, -1);
    }

    /**
     * 根据时间读取缓存
     *
     * @param type 保存的类型
     * @param key  缓存key
     * @param time 保存时间
     */
    public <T> Observable<T> load(final Type type, final String key, final long time) {
        return Observable.create(new SimpleSubscribe<T>() {
            @Override
            T execute() {
                return mCacheCore.load(type, key, time);
            }
        });
    }

    /**
     * 保存
     *
     * @param key   缓存key
     * @param value 缓存Value
     */
    public <T> Observable<Boolean> save(final String key, final T value) {
        return Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                mCacheCore.save(key, value);
                return true;
            }
        });
    }

    /**
     * 是否包含
     */
    public Observable<Boolean> containsKey(final String key) {
        return Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                return mCacheCore.containsKey(key);
            }
        });
    }

    /**
     * 删除缓存
     */
    public Observable<Boolean> remove(final String key) {
        return Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                return mCacheCore.remove(key);
            }
        });
    }

    /**
     * 清空缓存
     */
    public Observable<Boolean> clear() {
        return Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                return mCacheCore.clear();
            }
        });
    }
    /**
     * 关闭DiskCache
     */
    public Observable<Boolean> close() {
        return Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                return mCacheCore.close();
            }
        });
    }

    /**
     * 加载缓存策略模型
     */
    private IStrategy loadStrategy(CacheMode cacheMode) {
        IStrategy strategy = null;
        switch (cacheMode) {
            case NO_CACHE:
                strategy = new NoStrategy();
                break;
            case ONLY_CACHE:
                strategy = new OnlyCacheStrategy();
                break;
            case FIRST_CACHE:
                strategy = new FirstCacheStategy();
                break;
            case ONLY_REMOTE:
                strategy = new OnlyRemoteStrategy();
                break;
            case FIRST_REMOTE:
                strategy = new FirstRemoteStrategy();
                break;
            case CACHE_AND_REMOTE:
                strategy = new CacheAndRemoteStrategy();
                break;
            case CACHE_AND_REMOTE_DISTINCT:
                strategy = new CacheAndRemoteDistinctStrategy();
                break;
            default:
                break;
        }

        return strategy;
    }

    public long getCacheTime() {
        return mCacheTime;
    }

    public String getCacheKey() {
        return mCacheKey;
    }

    public Context getContext() {
        return mContext;
    }

    public CacheCore getCacheCore() {
        return mCacheCore;
    }

    public IDiskConverter getDiskConverter() {
        return mDiskConverter;
    }

    public File getDiskDir() {
        return mDiskDir;
    }

    public int getAppVersion() {
        return mAppVersion;
    }

    public long getDiskMaxSize() {
        return mDiskMaxSize;
    }

    public static final class Builder {

        private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB

        private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB

        public static final long CACHE_NEVER_EXPIRE = -1;//永久不过期

        private int appVersion;

        private long diskMaxSize;

        private File diskDir;

        private IDiskConverter diskConverter;

        private Context context;

        private String cachekey;

        private long cacheTime;

        public Builder() {
            diskConverter = new SerializableDiskConverter();
            cacheTime = CACHE_NEVER_EXPIRE;
            appVersion = 1;
        }

        public Builder(RxCache rxCache) {
            this.context = rxCache.mContext;
            this.appVersion = rxCache.mAppVersion;
            this.diskMaxSize = rxCache.mDiskMaxSize;
            this.diskDir = rxCache.mDiskDir;
            this.diskConverter = rxCache.mDiskConverter;
            this.cachekey = rxCache.mCacheKey;
            this.cacheTime = rxCache.mCacheTime;
        }

        public Builder init(Context context) {
            this.context = context;
            return this;
        }

        /**
         * 不设置，默认为1
         */
        public Builder appVersion(int appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        /**
         * 默认为缓存路径
         */
        public Builder diskDir(File directory) {
            this.diskDir = directory;
            return this;
        }


        public Builder diskConverter(IDiskConverter converter) {
            this.diskConverter = converter;
            return this;
        }

        /**
         * 不设置， 默为认50MB
         */
        public Builder diskMax(long maxSize) {
            this.diskMaxSize = maxSize;
            return this;
        }

        public Builder cachekey(String cachekey) {
            this.cachekey = cachekey;
            return this;
        }

        public Builder cacheTime(long cacheTime) {
            this.cacheTime = cacheTime;
            return this;
        }

        public RxCache build() {
            if (this.diskDir == null && this.context != null) {
                this.diskDir = getDiskCacheDir(this.context, "data-cache");
            }
            HttpUtil.checkNotNull(this.diskDir, "diskDir==null");
            if (!this.diskDir.exists()) {
                this.diskDir.mkdirs();
            }
            if (this.diskConverter == null) {
                this.diskConverter = new SerializableDiskConverter();
            }
            if (diskMaxSize <= 0) {
                diskMaxSize = calculateDiskCacheSize(diskDir);
            }
            cacheTime = Math.max(CACHE_NEVER_EXPIRE, this.cacheTime);

            appVersion = Math.max(1, this.appVersion);

            return new RxCache(this);
        }

        @SuppressWarnings("deprecation")
        private static long calculateDiskCacheSize(File dir) {
            long size = 0;
            try {
                StatFs statFs = new StatFs(dir.getAbsolutePath());
                long available = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
                size = available / 50;
            } catch (IllegalArgumentException ignored) {
            }
            return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE);
        }

        /**
         * 应用程序缓存原理：
         * 1.当SD卡存在或者SD卡不可被移除的时候，就调用getExternalCacheDir()方法来获取缓存路径，否则就调用getCacheDir()方法来获取缓存路径<br>
         * 2.前者是/sdcard/Android/data/<application package>/cache 这个路径<br>
         * 3.后者获取到的是 /data/data/<application package>/cache 这个路径<br>
         *
         * @param uniqueName 缓存目录
         */
        public File getDiskCacheDir(Context context, String uniqueName) {
            File cacheDir;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    || !Environment.isExternalStorageRemovable()) {
                cacheDir = context.getExternalCacheDir();
            } else {
                cacheDir = context.getCacheDir();
            }
            if (cacheDir == null) {// if cacheDir is null throws NullPointerException
                cacheDir = context.getCacheDir();
            }
            return new File(cacheDir.getPath() + File.separator + uniqueName);
        }

    }
}
