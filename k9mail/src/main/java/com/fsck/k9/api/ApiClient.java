package com.fsck.k9.api;

import com.fsck.k9.api.model.Authorize;
import com.fsck.k9.api.model.MainConfig;
import com.fsck.k9.api.model.User;
import com.fsck.k9.api.model.UserLogin;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by andreaputzu on 22/12/16.
 */

public interface ApiClient {

    @GET("1/config")
    Observable<MainConfig> getConfig();

    @GET("1/account/authorize")
    Observable<Authorize> getAuthorize(@Query("udid") String udid, @Query("app_id") String appid);

    @FormUrlEncoded
    @POST("1/account/user/login")
    Observable<UserLogin> postUserLogin(@Field("username") String username, @Field("password") String password);


}
