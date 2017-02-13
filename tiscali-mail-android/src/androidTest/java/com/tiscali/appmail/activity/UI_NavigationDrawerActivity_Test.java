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
import com.tiscali.appmail.view.K9PullToRefreshListView;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ListView;

import junit.framework.Assert;

/**
 * Created by andreaputzu on 09/02/17.
 */
@RunWith(AndroidJUnit4.class)
public class UI_NavigationDrawerActivity_Test {

    public static final int MESSAGE_LIST_STEP_NUMBER = 25;
    @Rule
    public ActivityTestRule<NavigationDrawerActivity> mActivityRule =
            new ActivityTestRule<>(NavigationDrawerActivity.class);

    @Test
    public void swipeOnBoardingTest() {
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
    }

    @Test
    public void setCredentialTest() {
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

    @Test
    public void selectTabMailTest() {
        onView(withId(R.id.menu_mail)).perform(click());

        K9PullToRefreshListView pullToRefreshListView = (K9PullToRefreshListView) mActivityRule
                .getActivity().findViewById(R.id.message_list);

        ListView listView = pullToRefreshListView.getRefreshableView();

        int count = listView.getCount();

        Assert.assertTrue(count >= MESSAGE_LIST_STEP_NUMBER);

        // listView.getAdapter().get

        // Error performing 'load adapter data' on view 'with id:
        // com.tiscali.appmail:id/message_list'.
        // onData(anything())
        // .inAdapterView(withId(R.id.message_list))
        // .atPosition(0).perform(click());

        // onData(instanceOf(String.class)).inAdapterView(withTag(false)) //
        // .atPosition(1) //
        // .perform(click());

        //// : Error performing 'load adapter data' on view 'is assignable from class: class
        //// android.widget.AdapterView'.
        // onData(is(instanceOf(MessageListFragment.MessageListAdapter.class)))
        // .atPosition(0)
        // .perform(click());

        // TO_LIST_COLUMN = 6

        // onData(hasEntry(equalTo(ListViewSample.ROW_TEXT), contains("Inizia il tuo viaggio")))
        // .onChildView(withId(R.id.rowToggleButton)).perform(click());


        // onData(withRowString(EmailProvider.MessageColumns.TO_LIST,
        // "testappios@tiscali.it")).perform(click());
        // onData(is(instanceOf(M)));
        // check(matches(isDisplayed()));

        // assertThat(count, is(equalTo(MESSAGE_LIST_STEP_NUMBER)));

        // onData(anything())
        // .inAdapterView(allOf(instanceOf(ListView.class), isDisplayed()))
        // .atPosition(0)
        // .perform(click());

        // onData(hasToString(startsWith("Inizia il tuo viaggio"))).perform(click());

        // onData(withRowString(5, "Tiscali Per Te
        // <info.commerciali@it.tiscali.com>")).perform(click());

        // onData(is(instanceOf(Cursor.class)), CursorMatchers.withRowString(SENDER_LIST,
        // is("Tiscali Per Te <info.commerciali@it.tiscali.com>")));
        // onData(anything()).inAdapterView(withContentDescription("Tiscali Per Te
        // <info.commerciali@it.tiscali.com>")).atPosition(0).perform(click());

    }


}
