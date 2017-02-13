package com.tiscali.appmail.activity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tiscali.appmail.R;
import com.tiscali.appmail.view.K9PullToRefreshListView;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
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
//        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());

        onView(withId(R.id.menu_mail)).perform(click());

        K9PullToRefreshListView pullToRefreshListView = (K9PullToRefreshListView) mActivityRule
                .getActivity().findViewById(R.id.message_list);

        ListView listView = pullToRefreshListView.getRefreshableView();


        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(3).perform(click());

        onView(withId(R.id.mail_next_btn)).perform(click());

        Espresso.pressBack();
        // int count = listView.getCount();
        //
        // Assert.assertTrue(count == MESSAGE_LIST_STEP_NUMBER);

        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(3)
                .onChildView(withId(R.id.selected_checkbox)).perform(click());

        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(4)
                .onChildView(withId(R.id.selected_checkbox)).perform(click());

//        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());


        onView(withId(R.id.mark_as_unread)).perform(click());
    }


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
