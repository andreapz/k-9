package com.tiscali.appmail.api;

import com.tiscali.appmail.api.model.Authorize;
import com.tiscali.appmail.api.model.MainConfig;
import com.tiscali.appmail.api.model.UserLogin;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by andreaputzu on 22/12/16.
 */

public interface ApiClient {

    String TISCALIAPP_BASEURL = "https://tiscaliapp-api.tiscali.it/";
    String TISCALIAPP_CONFIG_URL = "1/config";

    @GET("1/config")
    Observable<MainConfig> getConfig();

    @GET
    Observable<Authorize> getAuthorize(@Url String url, @Query("udid") String udid,
            @Query("app_id") String appid);

    @GET
    Observable<UserLogin> getMe(@Url String url);

    @FormUrlEncoded
    @POST
    Observable<UserLogin> postUserLogin(@Url String url, @Field("username") String username,
            @Field("password") String password);

    @FormUrlEncoded
    @POST
    Observable<> postPushActivate(@Url String url, @Field("otp") String otp);

    @FormUrlEncoded
    @POST
    Observable<> postPushRegister(@Url String url, @Field("pushtoken") String otp,
            @Field("platform") String platform, @Field("environment") String environment);

    @FormUrlEncoded
    @POST
    Observable<UserLogin> postSectionVisibility(@Url String url, @Field("section") String sectionId,
            @Field("value") boolean isSelected);

    @FormUrlEncoded
    @POST
    Observable<UserLogin> postSectionFave(@Url String url, @Field("section") String sectionId,
            @Field("value") boolean isSelected);


}
