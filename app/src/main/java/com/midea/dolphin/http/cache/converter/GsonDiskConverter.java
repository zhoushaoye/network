package com.midea.dolphin.http.cache.converter;


import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;



import com.midea.dolphin.http.json.JsonUtil;
import com.midea.dolphin.http.utils.HttpLogUtil;
import com.midea.dolphin.http.utils.HttpUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ConcurrentModificationException;

/**
 * <p>描述：GSON-数据转换器</p>
 * 1.GSON-数据转换器其实就是存储字符串的操作<br>
 * 2.如果你的Gson有特殊处理，可以自己创建一个，否则用默认。<br>
 * <p>
 * 优点：<br>
 * 相对于SerializableDiskConverter转换器，存储的对象不需要进行序列化（Serializable），<br>
 * 特别是一个对象中又包含很多其它对象，每个对象都需要Serializable，比较麻烦<br>
 * </p>
 * <p>
 * <p>
 * 缺点：<br>
 * 就是存储和读取都要使用Gson进行转换，object->String->Object的给一个过程，相对来说<br>
 * 每次都要转换性能略低，但是以现在的手机性能可以忽略不计了。<br>
 * </p>
 */
public class GsonDiskConverter implements IDiskConverter {
    private Gson mGson;

    public GsonDiskConverter() {
        this.mGson = JsonUtil.getGson();
    }

    public GsonDiskConverter(Gson gson) {
        HttpUtil.checkNotNull(gson, "gson ==null");
        this.mGson = gson;
    }

    @Override
    public <T> T load(InputStream source, Type type) {
        T value = null;
        try {
            TypeAdapter<?> adapter = mGson.getAdapter(TypeToken.get(type));
            JsonReader jsonReader = mGson.newJsonReader(new InputStreamReader(source));
            value = (T) adapter.read(jsonReader);
            //value = gson.fromJson(new InputStreamReader(source), type);
        } catch (JsonIOException | IOException | ConcurrentModificationException | JsonSyntaxException e) {
            HttpLogUtil.e(e.getMessage());
        } catch (Exception e){
            HttpLogUtil.e(e.getMessage());
        }finally {
            HttpUtil.close(source);
        }
        return value;
    }

    @Override
    public boolean writer(OutputStream sink, Object data) {
        try {
            String json = mGson.toJson(data);
            byte[] bytes = json.getBytes();
            sink.write(bytes, 0, bytes.length);
            sink.flush();
            return true;
        } catch (JsonIOException | JsonSyntaxException | ConcurrentModificationException | IOException e) {
            HttpLogUtil.e(e.getMessage());
        }catch (Exception e){
            HttpLogUtil.e(e.getMessage());
        } finally {
            HttpUtil.close(sink);
        }
        return false;
    }

}
