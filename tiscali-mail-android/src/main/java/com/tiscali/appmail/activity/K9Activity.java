package com.tiscali.appmail.activity;

import com.tiscali.appmail.activity.K9ActivityCommon.K9ActivityMagic;
import com.tiscali.appmail.activity.misc.SwipeGestureDetector.OnSwipeGestureListener;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;


public class K9Activity extends AppCompatActivity implements K9ActivityMagic {

    private K9ActivityCommon mBase;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        mBase = K9ActivityCommon.newInstance(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mBase.preDispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void setupGestureDetector(OnSwipeGestureListener listener) {
        mBase.setupGestureDetector(listener);
    }
}
