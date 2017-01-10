package com.fsck.k9.api;

import android.content.Context;
import android.util.Log;

import com.fsck.k9.Account;
import com.fsck.k9.Preferences;
import com.fsck.k9.api.model.Authorize;
import com.fsck.k9.api.model.MainConfig;
import com.fsck.k9.api.model.UserLogin;
import com.fsck.k9.error.RetrofitException;
import com.fsck.k9.error.RxErrorHandlingCallAdapterFactory;
import com.fsck.k9.preferences.Storage;
import com.fsck.k9.preferences.StorageEditor;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by andreaputzu on 22/12/16.
 */

public class ApiController {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_AUTHORIZED = "Authorized";
    private static final int RETRY_COUNT = 1;
    private static final String TAG = ApiController.class.getName();

    private static final int HTTP_ERROR_401 = 401;

    private static final String HEADER_APP_ID = "app_id";

    private static String mAuthorizedHeaderValue = "";
    private static String UUID = "123456";

    private MainConfig mMainConfig;
    private Authorize mAuthorize;
    private UserLogin mUserLogin;

    private final Context mContext;
    private final Preferences mPrefs;
    private final StorageEditor mEditor;
    private final Storage mStorage;
    private final Account mAccount;
    private ApiClient mApiClient;

    private ResponseHeaderInterceptor.ResponseHeaderListener headerListener = new ResponseHeaderInterceptor.ResponseHeaderListener(){
        @Override
        public void onHeadersIntercepted(Headers headers){
            String headerAuthValue = headers.get(HEADER_AUTHORIZED);
            if(headerAuthValue != null) {
                mAuthorizedHeaderValue = headerAuthValue;
            }
        }
    };

    private HttpLoggingInterceptor mLoggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

    private Interceptor mInterceptor = new Interceptor() {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header(HEADER_AUTHORIZATION, mAuthorizedHeaderValue+","+HEADER_APP_ID+"=\""+UUID+"\"");

            Request request = requestBuilder.build();
//            return chain.proceed(request);
            Response response = chain.proceed(request);

//            response.code();
//
//            int tryCount = 0;
//            if(!response.isSuccessful() && tryCount++ < 3) {
//                Log.d("intercept", "Request is not successful - " + request.method()+"@"+request.url()+" "+tryCount);
//                response = chain.proceed(request);
//            }

            ResponseBody responseBody = response.body();
            String responseBodyString = response.body().string();
            Response newResponse = response.newBuilder().body(ResponseBody.create(responseBody.contentType(), responseBodyString.getBytes())).build();

            Log.d(getClass().getName(),"URL:"+request.url().toString()+" Body:"+responseBodyString);
            if(response.isSuccessful()) {
                mEditor.putString(request.url().toString(), responseBodyString);
                mEditor.commit();
            }

            return newResponse;
        }
    };

    private OkHttpClient mHttpAuthorizeClient = new OkHttpClient.Builder()
            .addInterceptor(mLoggingInterceptor)
            .addInterceptor(mInterceptor)
            .addInterceptor(new ResponseHeaderInterceptor(headerListener))
            .build();

    private Retrofit mAuthorizeRetrofit = new Retrofit.Builder()
            .baseUrl(ApiClient.TISCALIAPP_BASEURL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .client(mHttpAuthorizeClient)
            .callbackExecutor(new Executor() {
                @Override
                public void execute(Runnable runnable) {
                    runnable.run();
                }
            })
            .build();

    private OkHttpClient mHttpApiClient = new OkHttpClient.Builder()
            .addInterceptor(mLoggingInterceptor)
            .addInterceptor(mInterceptor)
            .build();

    private Retrofit mApiRetrofit = new Retrofit.Builder()
            .baseUrl(ApiClient.TISCALIAPP_BASEURL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
            .client(mHttpApiClient)
            .callbackExecutor(new Executor() {
                @Override
                public void execute(Runnable runnable) {
                    runnable.run();
                }
            })
            .build();


    private ApiClient apiClient() {
        if(mApiClient == null) {
            mApiClient = mApiRetrofit.create(ApiClient.class);
        }
        return mApiClient;
    }

    public void getConfig(Subscriber<MainConfig> subscriber) {
        apiClient()
                .getConfig()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT)
                .subscribe(subscriber);
    }

    public Observable<Authorize> getAuthorize() {
        if(mMainConfig == null) {
            return null;
        }
        return mAuthorizeRetrofit
                .create(ApiClient.class)
                .getAuthorize(mMainConfig.getEndpoints().getAccountAuthorize().getUrl(), "udid","123456")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT);
    }

    public Observable<UserLogin> postUserLogin() {
        if(mMainConfig == null || mAccount == null) {
            return null;
        }
        return apiClient()
                .postUserLogin(mMainConfig.getEndpoints().getAccountUserLogin().getUrl(), mAccount.getEmail(), "123456")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT);
    }

    public Observable<UserLogin> getMe() {
        if(mMainConfig == null) {
            return null;
        }
        return apiClient()
                .getMe(mMainConfig.getEndpoints().getUserMe().getUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT);
    }



    public ApiController(Context context) {
        mContext = context;
        mPrefs = Preferences.getPreferences(mContext);
        mStorage = mPrefs.getStorage();
        mEditor = mStorage.edit();
        List<Account> accounts = mPrefs.getAccounts();
        mAccount = accounts.get(0);
        init();
    }

    public void init() {

        String mainConfig = mStorage.getString(ApiClient.TISCALIAPP_BASEURL+ApiClient.TISCALIAPP_CONFIG_URL, "");
        if(mainConfig.length() > 0) {
            Gson gson = new Gson();
            mMainConfig = gson.fromJson(mainConfig, MainConfig.class);
        }

        if(mMainConfig != null) {
            Observable<Authorize> authorize = getAuthorize();
            if(authorize != null) {
                authorizeApi(true);
//                authorize.concatMap(new Func1<Authorize, Observable<UserLogin>>() {
//                    @Override
//                    public Observable<UserLogin> call(Authorize authorize) {
//                        return postUserLogin();
//                    }
//                }).subscribe(new SubscriberUserLogin());
            }
        }

        getConfig(new Subscriber<MainConfig>() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e) {
                Log.i(TAG,"MainConfig ERROR: " + e);
            }

            @Override
            public void onNext(MainConfig mainConfig) {
                Log.i(TAG, "MainConfig OK");
                mMainConfig = mainConfig;
                if(mAuthorizedHeaderValue.length() == 0) {
                    authorizeApi(mUserLogin == null);
                }
            }
        });
    }

    private void authorizeApi(final boolean login) {
        Observable<Authorize> authorize = getAuthorize();
        if(authorize != null) {
            authorize.subscribe(new Subscriber<Authorize>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    Log.i(TAG, "Authorize ERROR");
                }

                @Override
                public void onNext(Authorize authorize) {
                    Log.i(TAG, "Authorize OK");
                    if(login) {
                        userLoginApi();
                    }
                }
            });
        }
    }

    private void meApi() {
        Observable<UserLogin> me = getMe();
        if(me != null) {
            me.subscribe(new SubscriberUserLogin());
        }
    }

    private void userLoginApi() {
        Observable<UserLogin> postUserLogin = postUserLogin();

        if(postUserLogin != null) {
            postUserLogin.subscribe(new SubscriberUserLogin());
        }
    }

    class SubscriberUserLogin extends Subscriber<UserLogin> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            if(e instanceof RetrofitException) {
                RetrofitException re = (RetrofitException) e;
                Log.i("APITEST","ERROR: "+re.getMessage());

                if(Integer.valueOf(re.getMessage()) == HTTP_ERROR_401) {


                }

            }
            Log.i("APITEST","ERROR: "+e.toString());
        }

        @Override
        public void onNext(UserLogin userLogin) {
            Log.i("APITEST","Username: "+userLogin.getUser().getAccount());
            mUserLogin = userLogin;
        }
    }

    public static class ResponseHeaderInterceptor implements Interceptor {
        public interface ResponseHeaderListener {
            public void onHeadersIntercepted(Headers headers);
        }

        private ResponseHeaderListener mListener;

        public ResponseHeaderInterceptor() {}

        public ResponseHeaderInterceptor(ResponseHeaderListener listener) {
            mListener = listener;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            if (mListener != null) {
                mListener.onHeadersIntercepted(response.headers());
            }
            return response;
        }
    }
}

