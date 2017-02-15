package com.tiscali.appmail.activity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tiscali.appmail.R;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

/**
 * Created by andreaputzu on 09/02/17.
 */
@RunWith(AndroidJUnit4.class)
public class UI_Startup_Test {

    @Rule
    public ActivityTestRule<NavigationDrawerActivity> mActivityRule =
            new ActivityTestRule<>(NavigationDrawerActivity.class);

    @Test
    public void startUpTest() {
        // 1
        onView(withId(R.id.btn_skip)).check(matches(not(isDisplayed())));
        onView(withId(R.id.view_pager)).perform(swipeLeft());
        // 2
        onView(withId(R.id.btn_skip)).check(matches(not(isDisplayed())));
        onView(withId(R.id.view_pager)).perform(swipeLeft());
        // 3
        onView(withId(R.id.btn_skip)).check(matches(not(isDisplayed())));
        onView(withId(R.id.view_pager)).perform(swipeLeft());
        // 4
        onView(withId(R.id.btn_skip)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_skip)).perform(click());

        onView(withId(R.id.account_email)).perform(click());
        onView(withId(R.id.account_email)).perform(typeText("testappios"));
        onView(withId(R.id.account_password)).perform(click());
        onView(withId(R.id.account_password)).perform(typeText("AppIos25"));
        onView(withId(R.id.show_password)).perform(click());
        onView(withId(R.id.show_password)).check(matches(isChecked()));
        onView(withId(R.id.next)).perform(click());

        onView(withId(R.id.account_name)).perform(click());
        onView(withId(R.id.account_name)).perform(typeText("Test Account"));
        onView(withId(R.id.done)).perform(click());
    }
}
