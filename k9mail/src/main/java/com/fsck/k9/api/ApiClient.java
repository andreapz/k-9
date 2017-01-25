package com.fsck.k9.api;

import com.fsck.k9.api.model.Authorize;
import com.fsck.k9.api.model.MainConfig;
import com.fsck.k9.api.model.UserLogin;

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
    Observable<UserLogin> postSectionVisibility(@Url String url, @Field("section") String sectionId,
            @Field("value") boolean isSelected);

    @FormUrlEncoded
    @POST
    Observable<UserLogin> postSectionFave(@Url String url, @Field("section") String sectionId,
            @Field("value") boolean isSelected);


}
