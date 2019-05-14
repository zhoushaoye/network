package com.midea.dolphin.http.apiservice;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 通用的的api接口
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public interface ApiService {

    /**
     *  post请求
     * @param url 请求地址,全链接或者baseUrl之后的半截
     * @param maps 请求参数
     */
    @POST()
    @FormUrlEncoded
    Observable<ResponseBody> post(@HeaderMap Map<String, String> headers, @Url String url, @FieldMap Map<String, String> maps);


    @POST()
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Observable<ResponseBody> postJson(@Url String url, @Body RequestBody jsonBody);

    /**
     *  上传一个自定义的请求体RequestBody
     * @param url  上传地址
     * @param body 请求体
     */
    @POST()
    Observable<ResponseBody> postBody(@HeaderMap Map<String, String> headers, @Url String url, @Body RequestBody body);

    /**
     * 上传Json
     * @param url 请求地址,全链接或者baseUrl之后的半截
     * @param jsonBody json
     */
    @POST()
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Observable<ResponseBody> postJson(@HeaderMap Map<String, String> headers, @Url String url, @Body RequestBody jsonBody);

    /**
     *  get请求
     * @param url 请求地址,全链接或者baseUrl之后的半截
     * @param maps 请求参数
     */
    @GET()
    Observable<ResponseBody> get(@HeaderMap Map<String, String> headers, @Url String url, @QueryMap Map<String, String> maps);

    /**
     *  put请求
     * @param url 请求地址,全链接或者baseUrl之后的半截
     * @param maps 请求参数
     */
    @PUT()
    Observable<ResponseBody> put(@HeaderMap Map<String, String> headers, @Url String url, @QueryMap Map<String, String> maps);

    /**
     *  put方式上传
     * @param url 上传地址
     * @param body 请求体
     */
    @PUT()
    Observable<ResponseBody> putBody(@HeaderMap Map<String, String> headers, @Url String url, @Body RequestBody body);

    /**
     *  delete请求
     * @param url 请求地址,全链接或者baseUrl之后的半截
     * @param maps 请求参数
     */
    @DELETE()
    Observable<ResponseBody> delete(@HeaderMap Map<String, String> headers, @Url String url, @QueryMap Map<String, String> maps);

    /**
     *  MultipartBody.Part上传
     * @param url 上传地址
     * @param parts 可带参文件对象
     */
    @Multipart
    @POST()
    Observable<ResponseBody> uploadFiles(@HeaderMap Map<String, String> headers, @Url String url, @Part() List<MultipartBody.Part> parts);

    /**
     *  RequestBody 方式上传
     * @param url 上传地址
     * @param maps 可带参请求体
     */
    @Multipart
    @POST()
    Observable<ResponseBody> uploadFiles(@HeaderMap Map<String, String> headers, @Url String url, @PartMap() Map<String, RequestBody> maps);

    /**
     * 文件下载
     * @param fileUrl 下载地址
     */
    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(@HeaderMap Map<String, String> headers, @Url String fileUrl);
}
