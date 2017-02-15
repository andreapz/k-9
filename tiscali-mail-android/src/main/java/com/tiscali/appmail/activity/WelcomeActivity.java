package com.tiscali.appmail.activity;

import javax.inject.Inject;

import com.bumptech.glide.Glide;
import com.tiscali.appmail.ApplicationComponent;
import com.tiscali.appmail.K9;
import com.tiscali.appmail.R;
import com.tiscali.appmail.api.ApiController;
import com.tiscali.appmail.api.model.DeviceRegister;
import com.tiscali.appmail.api.model.MainConfig;
import com.tiscali.appmail.api.model.Me;
import com.tiscali.appmail.api.model.Onboarding;
import com.tiscali.appmail.preferences.FirebasePreference;
import com.tiscali.appmail.preferences.WelcomePreference;
import com.tiscali.appmail.service.TiscaliAppFirebaseInstanceIDService;
import com.tiscali.appmail.service.TiscaliAppFirebaseMessagingService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import rx.functions.Action1;

/**
 * Created by thomascastangia on 31/01/17.
 */

public class WelcomeActivity extends AppCompatActivity
        implements ApiController.ApiControllerInterface {

    private ViewPager mViewPager;
    private int mNumPages = 2;
    private final String IMAGE_RESOURCE_BASE_NAME = "tutorial";
    private WelcomePagerAdapter myViewPagerAdapter;
    private LinearLayout mDotsLayout;
    private TextView[] mDots;
    private Button mBtnSkip;
    private WelcomePreference mPrefManager;
    private Onboarding mOnBoarding;
    private MainConfig mMainConfig;
    private int[] mPlaceholders;
    private BroadcastReceiver mBroadcastReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;

    @Inject
    ApiController mApiController;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildDaggerComponent(new Intent());
        // Checking for first time launch - before calling setContentView()
        mPrefManager = new WelcomePreference(this);
        if (!mPrefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_welcome);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mDotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        mBtnSkip = (Button) findViewById(R.id.btn_skip);


        mPlaceholders = getPlaceholders();

        // adding bottom mDots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new WelcomePagerAdapter();
        mViewPager.setAdapter(myViewPagerAdapter);
        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());

        mBtnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHomeScreen();
            }
        });

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                if (TiscaliAppFirebaseInstanceIDService.TOKEN_BROADCAST
                        .equals(intent.getAction())) {
                    if (intent.getStringExtra(
                            TiscaliAppFirebaseInstanceIDService.FIREBASE_PUSH_TOKEN) != null) {
                        String token = intent.getStringExtra(
                                TiscaliAppFirebaseInstanceIDService.FIREBASE_PUSH_TOKEN);
                        Log.i("APZ", "Push token: " + token);
                        mApiController.pushRegister(token,
                                TiscaliAppFirebaseInstanceIDService.FIREBASE_PLATFORM,
                                TiscaliAppFirebaseInstanceIDService.FIREBASE_ENVIRONMENT,
                                new Action1<DeviceRegister>() {
                                    @Override
                                    public void call(DeviceRegister register) {
                                        Log.i("APZ",
                                                "DeviceRegister Status: " + register.getStatus());
                                        Log.i("APZ",
                                                "DeviceRegister Device: " + register.getDevice());
                                    }
                                });
                    }
                } else if (TiscaliAppFirebaseMessagingService.TOKEN_VERIFY_BROADCAST
                        .equals(intent.getAction())) {
                    if (intent.getStringExtra(
                            TiscaliAppFirebaseMessagingService.FIREBASE_OTP_TOKEN) != null) {
                        String otp = intent.getStringExtra(
                                TiscaliAppFirebaseMessagingService.FIREBASE_OTP_TOKEN);
                        Log.i("APZ", "Push otp: " + otp);
                        mApiController.pushActivate(otp, new Action1<DeviceRegister>() {
                            @Override
                            public void call(DeviceRegister register) {
                                FirebasePreference.getInstance(getApplicationContext())
                                        .resetToken();
                                Log.i("APZ", "DeviceActivate Status: " + register.getStatus());
                                Log.i("APZ", "DeviceActivate Device: " + register.getDevice());
                            }
                        });
                    }
                }
            }
        };

    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        // mOnBoarding = mApiController.getMainConfig().getConfig().getOnboarding();
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver,
                new IntentFilter(TiscaliAppFirebaseInstanceIDService.TOKEN_BROADCAST));
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver,
                new IntentFilter(TiscaliAppFirebaseMessagingService.TOKEN_VERIFY_BROADCAST));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApiController.addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mApiController.removeListener(this);
    }

    @Override
    protected void onStop() {
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        super.onStop();
    }

    private void addBottomDots(int currentPage) {

        // Fixme change all with graphic version
        mDots = new TextView[mNumPages];

        int colorsActive = getResources().getColor(R.color.dot_dark_screen);
        int colorsInactive = getResources().getColor(R.color.dot_light_screen);

        mDotsLayout.removeAllViews();
        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(colorsInactive);
            mDotsLayout.addView(mDots[i]);
        }

        if (mDots.length > 0) {
            mDots[currentPage].setTextColor(colorsActive);
        }

    }

    private int getItem(int i) {
        return mViewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        mPrefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(WelcomeActivity.this, NavigationDrawerActivity.class));
        finish();
    }

    private void buildDaggerComponent(Intent intent) {
        ApplicationComponent component = ((K9) getApplicationContext()).getComponent();
        DaggerWelcomeActivityComponent.builder().applicationComponent(component)
                .activityModule(new ActivityModule(this, intent)).build().inject(this);
    }

    // viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener =
            new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageSelected(int position) {
                    addBottomDots(position);
                    if (mOnBoarding.getPages().get(position).getOptions().getAllowskip()) {
                        mBtnSkip.setVisibility(View.VISIBLE);
                        // changing the next button text 'NEXT' / 'GOT IT'
                        if (position == mNumPages - 1) {
                            // last page. make button text to GOT IT
                            mBtnSkip.setText(getString(R.string.start));
                        } else {
                            // still pages are left
                            mBtnSkip.setText(getString(R.string.skip));

                        }
                    } else {
                        mBtnSkip.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

                @Override
                public void onPageScrollStateChanged(int arg0) {

            }
            };

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public int[] getPlaceholders() {

        int[] placeholders = new int[mNumPages];
        for (int i = 0; i < placeholders.length; i++) {
            Resources resources = getResources();
            String name = IMAGE_RESOURCE_BASE_NAME + (i + 1);
            final int resourceId = resources.getIdentifier(name, "drawable", getPackageName());
            placeholders[i] = resourceId;
        }

        return placeholders;
    }

    @Override
    public void updateMe(Me me, String json) {

    }


    @Override
    public void updateMainConfig(MainConfig mainConfig) {
        mMainConfig = mainConfig;
        if (mMainConfig != null && mMainConfig.getConfig() != null) {
            mOnBoarding = mMainConfig.getConfig().getOnboarding();
            if (mOnBoarding != null) {
                mNumPages = mOnBoarding.getPages().size();
                mPlaceholders = getPlaceholders();
                addBottomDots(mViewPager.getCurrentItem());
                myViewPagerAdapter.notifyDataSetChanged();
                if (mOnBoarding.getPages().get(mViewPager.getCurrentItem()).getOptions()
                        .getAllowskip()) {
                    mBtnSkip.setVisibility(View.VISIBLE);
                } else {
                    mBtnSkip.setVisibility(View.GONE);
                }
            }
        }

    }

    /**
     * View pager adapter
     */
    public class WelcomePagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public WelcomePagerAdapter() {}

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.welcome_slide, container, false);
            ImageView pageImage = (ImageView) view.findViewById(R.id.slide);
            if (mOnBoarding != null) {
                Glide.with(getApplicationContext())
                        .load(mOnBoarding.getPages().get(position).getUrl())
                        .placeholder(mPlaceholders[position]).dontAnimate().into(pageImage);
            } else {
                Glide.with(getApplicationContext()).load("").placeholder(mPlaceholders[position])
                        .dontAnimate().into(pageImage);
            }

            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return mNumPages;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

    }
    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

}
