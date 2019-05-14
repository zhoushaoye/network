package com.midea.network.http.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;


/**
 * JsonUtils
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class JsonUtil {

    /**
     * 获取Gson实例
     *
     * @return gson
     */
    public static Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapterFactory(new NullTypeToEmptyAdapterFactory())
                .create();
    }

    /**
     * bean转成json
     *
     * @return json
     */
    public static String toJson(Object object) {
        return getGson().toJson(object);
    }


    public static <T> T fromJsonToBean(String jsonString, Type type) {
        return getGson().fromJson(jsonString, type);
    }
    public static <T> T fromJsonToBean(String jsonString, Class<T> cc) {
        return getGson().fromJson(jsonString, cc);
    }

    /**
     * json转成list
     */
    public static <T> List<T> fromJsonToList(String jsonString) {
        return getGson().fromJson(jsonString, new TypeToken<List<T>>() {
        }.getType());
    }

    /**
     * json转成map
     */
    public static <T> Map<String, T> fromJsonToMaps(String jsonString) {
        return getGson().fromJson(jsonString, new TypeToken<Map<String, T>>() {
        }.getType());
    }

}
