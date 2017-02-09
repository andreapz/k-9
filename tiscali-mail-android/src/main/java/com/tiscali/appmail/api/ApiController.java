package com.tiscali.appmail.api;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

import com.google.gson.Gson;
import com.tiscali.appmail.Account;
import com.tiscali.appmail.Preferences;
import com.tiscali.appmail.activity.setup.TiscaliAccountSetupUserPassword;
import com.tiscali.appmail.api.model.Authorize;
import com.tiscali.appmail.api.model.MainConfig;
import com.tiscali.appmail.api.model.Me;
import com.tiscali.appmail.api.model.UserLogin;
import com.tiscali.appmail.error.RetrofitException;
import com.tiscali.appmail.error.RxErrorHandlingCallAdapterFactory;
import com.tiscali.appmail.helper.CaptivePortalHelper;
import com.tiscali.appmail.preferences.Storage;
import com.tiscali.appmail.preferences.StorageEditor;

import android.app.Activity;
import android.util.Log;

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
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by andreaputzu on 22/12/16.
 */

public class ApiController {

    private static final String RESPONSE_ME = "RESPONSE_ME";
    private static final String TISCALIAPP_UUID = "TISCALIAPP_UUID";

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_AUTHORIZED = "Authorized";
    private static final int RETRY_COUNT = 1;
    private static final String TAG = ApiController.class.getName();

    private static final int HTTP_ERROR_401 = 401;
    private static final int HTTP_ERROR_403 = 403;
    private static final int HTTP_ERROR_404 = 404;

    private static final String HEADER_APP_ID = "app_id";

    private static String mAuthorizedHeaderValue = "";
    private static String UUID = "123456";
    private static String API_APPID = "1907198221081978";

    private MainConfig mMainConfig;
    private Authorize mAuthorize;
    private UserLogin mUserLogin;

    private final Activity mActivity;
    private final Preferences mPrefs;
    private final StorageEditor mEditor;
    private final Storage mStorage;
    private Account mAccount;

    private ApiClient mApiClient;

    private final Set<ApiControllerInterface> listeners = new CopyOnWriteArraySet<>();

    private ResponseHeaderInterceptor.ResponseHeaderListener headerListener =
            new ResponseHeaderInterceptor.ResponseHeaderListener() {
                @Override
                public void onHeadersIntercepted(Headers headers) {
                    String headerAuthValue = headers.get(HEADER_AUTHORIZED);
                    if (headerAuthValue != null) {
                        mAuthorizedHeaderValue = headerAuthValue;
                    }
                }
            };

    private HttpLoggingInterceptor mLoggingInterceptor =
            new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE);

    private Interceptor mInterceptor = new Interceptor() {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder().header(HEADER_AUTHORIZATION,
                    mAuthorizedHeaderValue + "," + HEADER_APP_ID + "=\"" + API_APPID + "\"");

            Request request = requestBuilder.build();
            // return chain.proceed(request);
            Response response = chain.proceed(request);

            // response.code();
            //
            // int tryCount = 0;
            // if(!response.isSuccessful() && tryCount++ < 3) {
            // Log.d("intercept", "Request is not successful - " +
            // request.method()+"@"+request.url()+" "+tryCount);
            // response = chain.proceed(request);
            // }

            ResponseBody responseBody = response.body();
            String responseBodyString = response.body().string();
            Response newResponse = response.newBuilder().body(
                    ResponseBody.create(responseBody.contentType(), responseBodyString.getBytes()))
                    .build();

            // Log.d(getClass().getName(),"URL:"+request.url().toString()+"
            // Body:"+responseBodyString);
            String urlKey = RESPONSE_ME;
            if (request.url().toString()
                    .equals(ApiClient.TISCALIAPP_BASEURL + ApiClient.TISCALIAPP_CONFIG_URL)) {
                urlKey = request.url().toString();
            }

            if (response.isSuccessful()) {
                mEditor.putString(urlKey, responseBodyString);
                mEditor.commit();
            }

            return newResponse;
        }
    };

    private OkHttpClient mHttpAuthorizeClient = new OkHttpClient.Builder()
            .addInterceptor(mLoggingInterceptor).addInterceptor(mInterceptor)
            .addInterceptor(new ResponseHeaderInterceptor(headerListener)).build();

    private Retrofit mAuthorizeRetrofit =
            new Retrofit.Builder().baseUrl(ApiClient.TISCALIAPP_BASEURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mHttpAuthorizeClient).callbackExecutor(new Executor() {
                        @Override
                        public void execute(Runnable runnable) {
                            runnable.run();
                        }
                    }).build();

    private OkHttpClient mHttpApiClient = new OkHttpClient.Builder()
            .addInterceptor(mLoggingInterceptor).addInterceptor(mInterceptor).build();

    private Retrofit mApiRetrofit = new Retrofit.Builder().baseUrl(ApiClient.TISCALIAPP_BASEURL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
            .client(mHttpApiClient).callbackExecutor(new Executor() {
                @Override
                public void execute(Runnable runnable) {
                    runnable.run();
                }
            }).build();


    private ApiClient apiClient() {
        if (mApiClient == null) {
            mApiClient = mApiRetrofit.create(ApiClient.class);
        }
        return mApiClient;
    }

    private Observable<MainConfig> getConfig() {
        return apiClient().getConfig().subscribeOn(Schedulers.io())
                // .observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT); // .subscribe(subscriber)
    }

    private Observable<Authorize> getAuthorize() {
        if (mMainConfig == null) {
            return null;
        }
        return mAuthorizeRetrofit.create(ApiClient.class)
                .getAuthorize(mMainConfig.getEndpoints().getAccountAuthorize().getUrl(), UUID,
                        API_APPID)
                .subscribeOn(Schedulers.io())
                // .observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT);
    }

    private Observable<UserLogin> postUserLogin() {
        if (mMainConfig == null || mAccount == null) {
            return null;
        }
        return apiClient()
                .postUserLogin(mMainConfig.getEndpoints().getAccountUserLogin().getUrl(),
                        mAccount.getEmail(), mAccount.getPassword())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT);
    }

    private Observable<UserLogin> postSectionVisibility(String sectionId, boolean isSelected) {
        if (mMainConfig == null) {
            return null;
        }
        return apiClient()
                .postSectionVisibility(mMainConfig.getEndpoints().getSectionVisibility().getUrl(),
                        sectionId, isSelected)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT);
    }

    private Observable<UserLogin> postSectionFave(String sectionId, boolean isSelected) {
        if (mMainConfig == null) {
            return null;
        }
        return apiClient()
                .postSectionFave(mMainConfig.getEndpoints().getSectionFave().getUrl(), sectionId,
                        isSelected)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT);
    }

    private Observable<UserLogin> getMe() {
        if (mMainConfig == null) {
            return null;
        }
        return apiClient().getMe(mMainConfig.getEndpoints().getUserMe().getUrl())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .retry(RETRY_COUNT);
    }


    public ApiController(Activity activity) {
        mActivity = activity;
        mPrefs = Preferences.getPreferences(activity);
        mStorage = mPrefs.getStorage();
        mEditor = mStorage.edit();
        List<Account> accounts = mPrefs.getAccounts();
        if (accounts != null && accounts.size() > 0) {
            mAccount = accounts.get(0);
        }
        UUID = mStorage.getString(TISCALIAPP_UUID, java.util.UUID.randomUUID().toString());
        init();
    }

    private void init() {

        String mainConfig = mStorage
                .getString(ApiClient.TISCALIAPP_BASEURL + ApiClient.TISCALIAPP_CONFIG_URL, "");
        if (mainConfig.length() > 0) {
            Gson gson = new Gson();
            mMainConfig = gson.fromJson(mainConfig, MainConfig.class);
            refreshMainConfigListeners();
        }

        if (mMainConfig != null) {
            Observable<Authorize> authorize = getAuthorize();
            if (authorize != null) {
                authorizeApi(true);
            }

            String userLogin = mStorage
                    .getString(mMainConfig.getEndpoints().getAccountUserLogin().getUrl(), "");
            if (userLogin.length() > 0) {
                Gson gson = new Gson();
                mUserLogin = gson.fromJson(userLogin, UserLogin.class);
            }
        }

        getConfig().subscribe(new Subscriber<MainConfig>() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e) {
                // test for Captive Portal
                if (CaptivePortalHelper.getInstance(mActivity).isCaptivePortalConnection()) {
                    // show the login activity
                    CaptivePortalHelper.getInstance(mActivity).showLoginWebView();
                }
                Log.i(TAG, "MainConfig ERROR: " + e);
            }

            @Override
            public void onNext(MainConfig mainConfig) {
                Log.i(TAG, "MainConfig OK");
                mMainConfig = mainConfig;
                if (mAuthorizedHeaderValue.length() == 0) {
                    authorizeApi(mUserLogin == null);
                }
                refreshMainConfigListeners();
            }
        });
    }

    private void authorizeApi(final boolean login) {
        Observable<Authorize> authorize = getAuthorize();
        if (authorize != null) {
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
                    if (login) {
                        userLoginApi();
                    }
                }
            });
        }
    }

    private void meApi() {
        Observable<UserLogin> me = getMe();
        if (me != null) {
            me.subscribe(new SubscriberUserLogin());
        }
    }

    private void userLoginApi() {
        Observable<UserLogin> postUserLogin = postUserLogin();

        if (postUserLogin != null) {
            postUserLogin.subscribe(new SubscriberUserLogin());
        }
    }

    public void sectionVisibility(String sectionId, boolean isSelected) {
        Observable<UserLogin> postSectionVisibility = postSectionVisibility(sectionId, isSelected);

        if (postSectionVisibility != null) {
            postSectionVisibility.subscribe(new SubscriberUserLogin());
        }
    }


    public void sectionFave(String sectionId, boolean isSelected, Action1<UserLogin> success,
            Action1<UserLogin> error) {
        Observable<UserLogin> postSectionFave = postSectionFave(sectionId, isSelected);

        if (postSectionFave != null) {
            postSectionFave.subscribe(new SubscriberMe(success, error));
        }
    }

    class SubscriberMe extends SubscriberUserLogin {
        private Action1<UserLogin> mSucces;
        private Action1<UserLogin> mError;

        public SubscriberMe(Action1<UserLogin> mSucces, Action1<UserLogin> mError) {
            this.mSucces = mSucces;
            this.mError = mError;
        }

        @Override
        public void onError(Throwable e) {
            super.onError(e);
            mError.call(null);
        }

        @Override
        public void onNext(UserLogin userLogin) {
            super.onNext(userLogin);
            mSucces.call(userLogin);
        }
    }

    class SubscriberUserLogin extends Subscriber<UserLogin> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            if (e instanceof RetrofitException) {
                RetrofitException re = (RetrofitException) e;
                Log.i("APITEST", "ERROR: " + re.getMessage());

                if (Integer.valueOf(re.getMessage()) == HTTP_ERROR_401) {

                    Observable<Authorize> authorize = getAuthorize();
                    if (authorize != null) {
                        authorize.concatMap(new Func1<Authorize, Observable<UserLogin>>() {
                            @Override
                            public Observable<UserLogin> call(Authorize authorize) {
                                return postUserLogin();
                            }
                        }).subscribe(new SubscriberUserLogin());
                    }
                } else if (Integer.valueOf(re.getMessage()) == HTTP_ERROR_403) {
                    mAccount.setPassword("");
                    mAccount.save(mPrefs);
                    TiscaliAccountSetupUserPassword.actionEditUserPasswordSettings(mActivity,
                            mAccount);
                } else if (Integer.valueOf(re.getMessage()) == HTTP_ERROR_404) {

                    Observable<MainConfig> config = getConfig();
                    if (config != null) {
                        config.concatMap(new Func1<MainConfig, Observable<Authorize>>() {
                            @Override
                            public Observable<Authorize> call(MainConfig mainConfig) {
                                mMainConfig = mainConfig;
                                refreshMainConfigListeners();
                                return getAuthorize();
                            }
                        }).concatMap(new Func1<Authorize, Observable<UserLogin>>() {
                            @Override
                            public Observable<UserLogin> call(Authorize authorize) {
                                return postUserLogin();
                            }
                        }).subscribe(new SubscriberUserLogin());
                    }
                }

            }
            Log.i("APITEST", "ERROR: " + e.toString());
        }

        @Override
        public void onNext(UserLogin userLogin) {
            Log.i("APITEST", "Username: " + userLogin.getUser().getAccount());
            mUserLogin = userLogin;
            refreshMeListeners();
        }
    }

    private static class ResponseHeaderInterceptor implements Interceptor {
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

    public void sendMe(ApiControllerInterface listener) {
        if (mMainConfig == null) {
            return;
        }

        String json = mStorage.getString(mMainConfig.getEndpoints().getUserMe().getUrl(), "");
        if (json.length() == 0) {
            json = mStorage.getString(RESPONSE_ME, "");
        }

        if (mUserLogin != null && mUserLogin.getMe() != null) {
            listener.updateMe(mUserLogin.getMe(), json);
        }
    }

    private void sendMainConfig(ApiControllerInterface listener) {
        listener.updateMainConfig(mMainConfig);
    }

    public void addListener(ApiControllerInterface listener) {
        listeners.add(listener);
        sendMe(listener);
        sendMainConfig(listener);
    }

    public MainConfig getMainConfig() {
        return mMainConfig;
    }

    public void removeListener(ApiControllerInterface listener) {
        listeners.remove(listener);
    }

    public Set<ApiControllerInterface> getListeners() {
        return listeners;
    }


    public Set<ApiControllerInterface> getListeners(ApiControllerInterface listener) {
        if (listener == null) {
            return listeners;
        }

        Set<ApiControllerInterface> listeners = new HashSet<>(this.listeners);
        listeners.add(listener);
        return listeners;

    }

    public interface ApiControllerInterface {
        void updateMe(Me me, String json);

        void updateMainConfig(MainConfig mainConfig);
    }

    public void refreshMeListeners() {
        for (ApiControllerInterface listener : listeners) {
            sendMe(listener);
        }
    }

    public void refreshMainConfigListeners() {
        for (ApiControllerInterface listener : listeners) {
            sendMainConfig(listener);
        }
    }

}

