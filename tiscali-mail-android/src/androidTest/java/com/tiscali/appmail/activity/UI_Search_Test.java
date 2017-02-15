package com.tiscali.appmail.activity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tiscali.appmail.R;

import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by thomascastangia on 15/02/17.
 */

@RunWith(AndroidJUnit4.class)
public class UI_Search_Test {

    @Rule
    public ActivityTestRule<NavigationDrawerActivity> mActivityRule =
            new ActivityTestRule<>(NavigationDrawerActivity.class);


    @Test
    public void searchMailWithResultTest() {

        // Select Tab Mail
        onView(withId(R.id.menu_mail)).perform(click());
        // Select Search icon
        onView(withId(R.id.search)).perform(click());

        // EditText search input & enter for search
        onView(isAssignableFrom(EditText.class)).perform(typeText("tiscali"),
                pressKey(KeyEvent.KEYCODE_ENTER));

        SystemClock.sleep(1000);
        // Select 2nd mail
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(2).perform(click());

        Espresso.pressBack();

        Espresso.pressBack();


    }

    @Test
    public void searchMailEmptyTest() {


        // Select Tab Mail
        onView(withId(R.id.menu_mail)).perform(click());

        // Select Search icon
        onView(withId(R.id.search)).perform(click());

        // EditText search input & enter for search
        onView(isAssignableFrom(EditText.class)).perform(typeText("zzz"),
                pressKey(KeyEvent.KEYCODE_ENTER));

        SystemClock.sleep(1000);

//        K9PullToRefreshListView pullToRefreshListView = (K9PullToRefreshListView) mActivityRule
//                .getActivity().findViewById(R.id.message_list);
//
//        ListView listView = pullToRefreshListView.getRefreshableView();
//
//
//        int count = listView.getCount();
//
//        Assert.assertTrue(count == 0);

        Espresso.pressBack();


    }
}
