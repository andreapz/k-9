package com.fsck.k9.api;

import com.fsck.k9.api.model.Config;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by andreaputzu on 22/12/16.
 */

public interface MainConfigurationApiClient {

    @GET("/1/config")
    Observable<Config> getConfig();
}
