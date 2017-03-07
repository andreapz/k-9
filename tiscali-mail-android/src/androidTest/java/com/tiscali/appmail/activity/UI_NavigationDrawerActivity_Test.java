package com.tiscali.appmail.activity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tiscali.appmail.R;
import com.tiscali.appmail.activity.setup.AccountSetupCheckSettings;
import com.tiscali.appmail.view.K9PullToRefreshListView;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by andreaputzu on 09/02/17.
 */
@RunWith(AndroidJUnit4.class)
public class UI_NavigationDrawerActivity_Test {

    public static final int MESSAGE_LIST_STEP_NUMBER = 25;
    @Rule
    public ActivityTestRule<NavigationDrawerActivity> mActivityRule =
            new ActivityTestRule<>(NavigationDrawerActivity.class);

    private UiDevice mDevice;

    @Before
    public void setUp() throws Exception {
        // super.setUp();
        mDevice = UiDevice.getInstance(getInstrumentation());
    }

    @Test
    public void selectTabMailTest() {
        // openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());

        // Select Tab Mail
        onView(withId(R.id.menu_mail)).perform(click());

        K9PullToRefreshListView pullToRefreshListView = (K9PullToRefreshListView) mActivityRule
                .getActivity().findViewById(R.id.message_list);

        ListView listView = pullToRefreshListView.getRefreshableView();

        // Select 3rd mail
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(3).perform(click());

        // next email from next button
        onView(withId(R.id.mail_next_btn)).perform(click());

        // back to the mail list
        Espresso.pressBack();
        // int count = listView.getCount();
        //
        // Assert.assertTrue(count == MESSAGE_LIST_STEP_NUMBER);

        // select 3rd and 4th mail
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(3)
                .onChildView(withId(R.id.selected_checkbox)).perform(click());

        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(4)
                .onChildView(withId(R.id.selected_checkbox)).perform(click());

        // openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // tap unread icon and force selected mail as unread
        onView(withId(R.id.mark_as_unread)).perform(click());
    }

    @Test
    public void addAccountWithSimplePassword() {

        final Instrumentation.ActivityMonitor activityMonitor = getInstrumentation()
                .addMonitor(AccountSetupCheckSettings.class.getName(), null, false);
        onView(withId(R.id.menu_mail)).perform(click());


        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))) // Left Drawer
                // should be
                // closed.
                .perform(open()); // Open Drawer

        onView(withId(R.id.expand_menu)).perform(click());

        onView(withId(R.id.left_drawer))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        onView(withId(R.id.account_email)).perform(click());
        onView(withId(R.id.account_email)).perform(typeText("testappmail"));
        onView(withId(R.id.account_password)).perform(click());
        onView(withId(R.id.account_password)).perform(typeText("123456"));
        onView(withId(R.id.show_password)).perform(click());
        onView(withId(R.id.show_password)).check(matches(isChecked()));

        onView(withId(R.id.next)).perform(click());

        AccountSetupCheckSettings nextActivity = (AccountSetupCheckSettings) getInstrumentation()
                .waitForMonitorWithTimeout(activityMonitor, 4000);
        assertNotNull(nextActivity);
        onView(withText(R.string.account_password_too_simple_failed_dlg_edit_details_action))
                .inRoot(withDecorView(not(is(nextActivity.getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
        onView(withText(R.string.account_password_too_simple_failed_dlg_edit_details_action))
                .inRoot(withDecorView(not(is(nextActivity.getWindow().getDecorView()))))
                .perform(click());

        Espresso.pressBack();
        Espresso.pressBack();


    }

    @Test
    public void addSecondAccountPassword() {
        onView(withId(R.id.menu_mail)).perform(click());


        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))) // Left Drawer
                // should be
                // closed.
                .perform(open()); // Open Drawer

        onView(withId(R.id.expand_menu)).perform(click());

        onView(withId(R.id.left_drawer))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        onView(withId(R.id.account_email)).perform(click());
        onView(withId(R.id.account_email)).perform(typeText("thomascas1974"));
        onView(withId(R.id.account_password)).perform(click());
        onView(withId(R.id.account_password)).perform(typeText("Thom1974"));
        onView(withId(R.id.show_password)).perform(click());
        onView(withId(R.id.show_password)).check(matches(isChecked()));
        onView(withId(R.id.next)).perform(click());

        onView(withId(R.id.account_name)).perform(click());
        onView(withId(R.id.account_name)).perform(typeText("Furrillu Account"));
        onView(withId(R.id.done)).perform(click());
    }

    @Test
    public void changeVisibilityNewsSection() {

        onView(withId(R.id.menu_news)).perform(click());

        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))) // Left Drawer
                // should be
                // closed.
                .perform(open()); // Open Drawer

        onView(withText(R.string.menu_item_customize)).perform(click());

        // Select 3rd and 1st voice
        onData(anything()).inAdapterView(withId(R.id.list_category)).atPosition(3)
                .onChildView(withId(R.id.toggle_media)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.list_category)).atPosition(1)
                .onChildView(withId(R.id.toggle_media)).perform(click());

        SystemClock.sleep(1000);

        Espresso.pressBack();
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))) // Left Drawer
                // should be
                // closed.
                .perform(open()); // Open Drawer

        onView(withText(R.string.menu_item_customize)).perform(click());

        // Select 3rd and 1st voice
        onData(anything()).inAdapterView(withId(R.id.list_category)).atPosition(3)
                .onChildView(withId(R.id.toggle_media)).check(matches(isNotChecked()));
        onData(anything()).inAdapterView(withId(R.id.list_category)).atPosition(1)
                .onChildView(withId(R.id.toggle_media)).check(matches(isNotChecked()));
        Espresso.pressBack();

    }

    @Test
    public void shareInformationPage() {

        onView(withId(R.id.menu_news)).perform(click());

        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))) // Left Drawer
                // should be
                // closed.
                .perform(open()); // Open Drawer

        onView(withId(R.id.settings)).perform(click());

        onView(withText(R.string.informations_action)).perform(click());

        onView(withId(R.id.menu_item_share)).perform(click());

        mDevice.pressBack();
        mDevice.pressBack();


    }

    @Test
    public void scrollDownAndUp() {
        onView(withId(R.id.menu_mail)).perform(click());
        K9PullToRefreshListView pullToRefreshListView = (K9PullToRefreshListView) mActivityRule
                .getActivity().findViewById(R.id.message_list);

        ListView listView = pullToRefreshListView.getRefreshableView();

        int count = listView.getCount();
        // Scroll message list
        if (count < MESSAGE_LIST_STEP_NUMBER) {
            listView.smoothScrollToPosition(count);
        } else {
            listView.smoothScrollToPosition(MESSAGE_LIST_STEP_NUMBER);
        }


        SystemClock.sleep(1000);
        onView(withId(R.id.bottom_navigation)).check(matches(not(isDisplayed())));
        listView.smoothScrollToPosition(0);

        SystemClock.sleep(1000);
        onView(withId(R.id.bottom_navigation)).check(matches((isDisplayed())));

    }

    @Test
    public void openSubSectionNewsDetail() {

        onView(withId(R.id.menu_news)).perform(click());
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))) // Left Drawer
                // should be
                // closed.
                .perform(open()); // Open Drawer

        onView(withId(R.id.left_drawer))
                .perform(RecyclerViewActions.actionOnItemAtPosition(9, click()));

        SystemClock.sleep(2000);

        onView(withId(R.id.left_drawer)).perform(scrollToPosition(14));

        SystemClock.sleep(2000);
        onView(withId(R.id.left_drawer))
                .perform(RecyclerViewActions.actionOnItemAtPosition(11, click()));


        SystemClock.sleep(2000);

        // onData(anything()).inAdapterView(withId(R.id.list_category)).atPosition(8).onChildView(withId(R.id.toggle_media)).perform(click());

        onWebView(withIndex(withId(R.id.webview), 0)).forceJavascriptEnabled();

        SystemClock.sleep(2000);

        onView(withIndex(withId(R.id.webview), 0)).check(matches(isDisplayed()));
        //
        onView(withIndex(withId(R.id.webview), 0)).perform(clickXY(200, 400));
        //
        mDevice.pressBack();
        mDevice.pressBack();


    }

    @Test
    public void shareVideoDetail() {
        onView(withId(R.id.menu_video)).perform(click());
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))) // Left Drawer
                // should be
                // closed.
                .perform(open()); // Open Drawer

        onView(withId(R.id.left_drawer))
                .perform(RecyclerViewActions.actionOnItemAtPosition(5, click()));

        onWebView(withIndex(withId(R.id.webview), 0)).forceJavascriptEnabled();

        SystemClock.sleep(2000);

        onView(withIndex(withId(R.id.webview), 1)).check(matches(isDisplayed()));

        onView(withIndex(withId(R.id.webview), 1)).perform(clickXY(200, 400));

        onView(withId(R.id.menu_item_share)).perform(click());

        mDevice.pressBack();
        mDevice.pressBack();


    }

    @Test
    public void mailOptionMenuOrder() {

        // test mailOptionMenuOrderAtFolder Inbox
        mailOptionMenuOrderAtFolder(1);
        // test mailOptionMenuOrderAtFolder Bozze
        mailOptionMenuOrderAtFolder(2);
        // test mailOptionMenuOrderAtFolder In Uscita
        mailOptionMenuOrderAtFolder(3);
        // test mailOptionMenuOrderAtFolder Inviata
        mailOptionMenuOrderAtFolder(4);
        // test mailOptionMenuOrderAtFolder Spam
        mailOptionMenuOrderAtFolder(5);
        // test mailOptionMenuOrderAtFolder Trash
        mailOptionMenuOrderAtFolder(6);
        // test mailOptionMenuOrderAtFolder Archive
        mailOptionMenuOrderAtFolder(7);
    }

    public void mailOptionMenuOrderAtFolder(int position) {

        onView(withId(R.id.menu_mail)).perform(click());

        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.LEFT))) // Left Drawer
                // should be
                // closed.
                .perform(open()); // Open Drawer

        onView(withId(R.id.left_drawer))
                .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));


        // verify voice sort by Date

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());


        SystemClock.sleep(1000);
        onView(withText(R.string.sort_by)).perform(click());
        onView(withText(R.string.sort_by_date)).perform(click());
        SystemClock.sleep(1000);

        // verify voice sort by Arrive time

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        SystemClock.sleep(1000);
        onView(withText(R.string.sort_by)).perform(click());
        onView(withText(R.string.sort_by_arrival)).perform(click());
        SystemClock.sleep(1000);


        // verify voice sort by Subject

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        SystemClock.sleep(1000);
        onView(withText(R.string.sort_by)).perform(click());
        onView(withText(R.string.sort_by_subject)).perform(click());
        SystemClock.sleep(1000);

        // verify voice sort by Sender

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        SystemClock.sleep(1000);
        onView(withText(R.string.sort_by)).perform(click());
        onView(withText(R.string.sort_by_sender)).perform(click());
        SystemClock.sleep(1000);

        // verify voice sort by Unread

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        SystemClock.sleep(1000);
        onView(withText(R.string.sort_by)).perform(click());
        onView(withText(R.string.sort_by_unread)).perform(click());
        SystemClock.sleep(1000);

        // verify voice sort by flag

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        SystemClock.sleep(1000);
        onView(withText(R.string.sort_by)).perform(click());
        onView(withText(R.string.sort_by_flag)).perform(click());
        SystemClock.sleep(1000);

        // verify voice sort by attach

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        SystemClock.sleep(1000);
        onView(withText(R.string.sort_by)).perform(click());
        onView(withText(R.string.sort_by_attach)).perform(click());
        SystemClock.sleep(1000);



    }

    @Test
    public void mailOptionMenuSelectAllAndDelete() {

        onView(withId(R.id.menu_mail)).perform(click());


        // verify voice select all and delete

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        SystemClock.sleep(1000);
        onView(withText(R.string.batch_select_all)).perform(click());

        SystemClock.sleep(1000);

        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(1).perform(click());
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(2).perform(click());


        SystemClock.sleep(1000);
        onView(withId(R.id.delete)).perform(click());

        // verify mark all as read

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        SystemClock.sleep(1000);
        onView(withText(R.string.batch_select_all)).perform(click());
        onView(withText(R.string.mark_all_as_read)).perform(click());

        mDevice.pressBack();


    }

    @Test
    public void mailOptionMenuSelectAllAndMarkAsRead() {

        onView(withId(R.id.menu_mail)).perform(click());


        // verify voice select all and delete

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        SystemClock.sleep(1000);
        onView(withText(R.string.batch_select_all)).perform(click());


        SystemClock.sleep(1000);

        onView(withId(R.id.mark_as_read)).perform(click());


        mDevice.pressBack();


    }

    @Test
    public void mailOptionMenuSelectAllAndArchive() {

        onView(withId(R.id.menu_mail)).perform(click());


        // verify voice select all and delete

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        SystemClock.sleep(1000);
        onView(withText(R.string.batch_select_all)).perform(click());

        SystemClock.sleep(1000);

        onView(withId(R.id.search)).perform(click());

        mDevice.pressBack();


    }

    // @Test
    // public void mailOptionMenuSelectAllAndRemoveFlag() throws UiObjectNotFoundException {
    //
    // onView(withId(R.id.menu_mail)).perform(click());
    //
    //
    // // verify voice select all and delete
    //
    // openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
    //
    // SystemClock.sleep(1000);
    // onView(withText(R.string.batch_select_all)).perform(click());
    //
    // SystemClock.sleep(1000);
    //
    //
    // openContextualActionModeOverflowMenu();
    //
    // SystemClock.sleep(1000);
    //
    // onView(withText(R.string.unflag_action)).perform(click());
    //
    //
    //
    // }


    public static Matcher<View> nthChildOf(final Matcher<View> parentMatcher,
            final int childPosition) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description
                        .appendText("with " + childPosition + " child view of type parentMatcher");
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view.getParent() instanceof ViewGroup)) {
                    return parentMatcher.matches(view.getParent());
                }

                ViewGroup group = (ViewGroup) view.getParent();
                return parentMatcher.matches(view.getParent())
                        && group.getChildAt(childPosition).equals(view);
            }
        };
    }

    // public static ViewAction customClick() {
    // return actionWithAssertions(new CustomGeneralClickAction(Tap.SINGLE,
    // GeneralLocation.VISIBLE_CENTER, Press.FINGER));
    // }

    public static ViewAction clickXY(final int x, final int y) {
        return new GeneralClickAction(Tap.SINGLE, new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {

                final int[] screenPos = new int[2];
                view.getLocationOnScreen(screenPos);

                final float screenX = screenPos[0] + x;
                final float screenY = screenPos[1] + y;
                float[] coordinates = {screenX, screenY};

                return coordinates;
            }
        }, Press.FINGER);
    }

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }


}
