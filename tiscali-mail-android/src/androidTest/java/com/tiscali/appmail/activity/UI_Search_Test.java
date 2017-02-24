package com.tiscali.appmail.activity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.anything;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tiscali.appmail.R;
import com.tiscali.appmail.provider.TiscaliSearchRecentSuggestionsProvider;
import com.tiscali.appmail.view.K9PullToRefreshListView;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.SystemClock;
import android.provider.SearchRecentSuggestions;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.widget.ListView;

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
        SearchRecentSuggestions suggestions =
                new SearchRecentSuggestions(InstrumentationRegistry.getTargetContext(),
                        TiscaliSearchRecentSuggestionsProvider.AUTHORITY,
                        TiscaliSearchRecentSuggestionsProvider.MODE);
        suggestions.clearHistory();
        // Select Search icon
        onView(withId(R.id.search)).perform(click());

        SystemClock.sleep(2000);


        // EditText search input & enter for search
        onView(withId(android.support.design.R.id.search_src_text)).perform(click())
                .perform(replaceText("tiscali"));



        onView(withId(android.support.design.R.id.search_src_text)).perform(pressImeActionButton());



        // Select 2nd mail
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(2).perform(click());



        Espresso.pressBack();



    }

    @Test
    public void searchMailEmptyTest() {

        Instrumentation.ActivityMonitor activityMonitor =
                getInstrumentation().addMonitor(Search.class.getName(), null, false);

        // Select Tab Mail
        onView(withId(R.id.menu_mail)).perform(click());

        SearchRecentSuggestions suggestions =
                new SearchRecentSuggestions(InstrumentationRegistry.getTargetContext(),
                        TiscaliSearchRecentSuggestionsProvider.AUTHORITY,
                        TiscaliSearchRecentSuggestionsProvider.MODE);
        suggestions.clearHistory();

        // Select Search icon
        onView(withId(R.id.search)).perform(click());

        // EditText search input & enter for search
        onView(withId(android.support.design.R.id.search_src_text)).perform(click())
                .perform(replaceText("zzz"));

        onView(withId(android.support.design.R.id.search_src_text)).perform(pressImeActionButton());

        Search nextActivity =
                (Search) getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 4000);

        assertNotNull(nextActivity);


        K9PullToRefreshListView pullToRefreshListView =
                (K9PullToRefreshListView) nextActivity.findViewById(R.id.message_list);

        ListView listView = pullToRefreshListView.getRefreshableView();

        int count = listView.getCount();
        // default count with empty list is 2 (header footer)
        Assert.assertTrue(count == 2);

        Espresso.pressBack();


    }

    public Activity getActivityInstance() {
        final Activity[] activity = new Activity[1];
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Activity currentActivity = null;
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(RESUMED);
                if (resumedActivities.iterator().hasNext()) {
                    currentActivity = (Activity) resumedActivities.iterator().next();
                    activity[0] = currentActivity;
                }
            }
        });

        return activity[0];
    }
}
