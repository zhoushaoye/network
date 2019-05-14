package com.midea.network.http.json;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;


/**
 * Json数据适配器
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class NullTypeToEmptyAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create (Gson gson, TypeToken<T> type) {
        Class<T> rawType = (Class<T>) type.getRawType();
        if (rawType == String.class) {
            return (TypeAdapter<T>) new StringNullAdapter();
        } else if (rawType == Integer.class) {
            return (TypeAdapter<T>) new IntegerNullAdapter();
        } else if (rawType == Double.class) {
            return (TypeAdapter<T>) new DoubleNullAdapter();
        } else if (rawType == Float.class) {
            return (TypeAdapter<T>) new FloatNullAdapter();
        } else if (rawType == Long.class) {
            return (TypeAdapter<T>) new LongNullAdapter();
        }
        return null;
    }

    class StringNullAdapter extends TypeAdapter<String> {

        @Override
        public String read (JsonReader reader) throws IOException {
            String str = "";
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return str;
            }
            try {
                str = reader.nextString();
            } catch (Exception e) {
                str = JsonUtil.getGson().toJson(new JsonParser().parse(reader));
            }
            return str;
        }

        @Override
        public void write (JsonWriter writer, String value) throws IOException {
//            if (value == null) {
//                writer.nullValue();
//                return;
//            }
            writer.value(value);
        }
    }

    class IntegerNullAdapter extends TypeAdapter<Integer> {

        @Override
        public void write (JsonWriter out, Integer value) throws IOException {
            out.value(value);
        }

        @Override
        public Integer read (JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return 0;
            }
            return in.nextInt();
        }
    }

    class DoubleNullAdapter extends TypeAdapter<Double> {

        @Override
        public void write (JsonWriter out, Double value) throws IOException {
            out.value(value);
        }

        @Override
        public Double read (JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return 0.0;
            }
            return in.nextDouble();
        }
    }

    class LongNullAdapter extends TypeAdapter<Long> {

        @Override
        public void write (JsonWriter out, Long value) throws IOException {
            out.value(value);
        }

        @Override
        public Long read (JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return 0L;
            }
            return in.nextLong();
        }
    }

    class FloatNullAdapter extends TypeAdapter<Float> {

        @Override
        public void write (JsonWriter out, Float value) throws IOException {
            out.value(value);
        }

        @Override
        public Float read (JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return 0f;
            }
            return Float.parseFloat(String.valueOf(in.nextDouble()));
        }
    }

}
