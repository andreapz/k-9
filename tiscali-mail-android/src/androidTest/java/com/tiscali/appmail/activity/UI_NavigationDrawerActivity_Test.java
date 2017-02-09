package com.tiscali.appmail.activity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tiscali.appmail.R;
import com.tiscali.appmail.view.K9PullToRefreshListView;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.ListView;

import junit.framework.Assert;

/**
 * Created by andreaputzu on 09/02/17.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UI_NavigationDrawerActivity_Test {

    public static final int MESSAGE_LIST_STEP_NUMBER = 25;
    @Rule
    public ActivityTestRule<NavigationDrawerActivity> mActivityRule =
            new ActivityTestRule<>(NavigationDrawerActivity.class);

    @Test
    public void selectTabMail() {
        onView(withId(R.id.menu_mail)).perform(click());

        K9PullToRefreshListView pullToRefreshListView = (K9PullToRefreshListView) mActivityRule
                .getActivity().findViewById(R.id.message_list);

        ListView listView = pullToRefreshListView.getRefreshableView();

        int count = listView.getCount();

        Assert.assertTrue(count >= MESSAGE_LIST_STEP_NUMBER);

        onData(anything()).inAdapterView(withId(R.id.message_list)).atPosition(1).perform(click());
        //check(matches(isDisplayed()));

        //assertThat(count, is(equalTo(MESSAGE_LIST_STEP_NUMBER)));
    }


}
