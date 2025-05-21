package ch.petrce.triolingo;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void mainActivityTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5622);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction viewGroup = onView(
                allOf(withId(R.id.main),
                        withParent(allOf(withId(android.R.id.content),
                                withParent(withId(com.google.android.material.R.id.action_bar_root)))),
                        isDisplayed()));
        viewGroup.check(matches(isDisplayed()));

        ViewInteraction textView = onView(
                allOf(withId(R.id.progressText), withText("1 / 30"),
                        withParent(allOf(withId(R.id.main),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        textView.check(matches(isDisplayed()));

        ViewInteraction progressBar = onView(
                allOf(withId(R.id.progressBar),
                        withParent(allOf(withId(R.id.main),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        progressBar.check(matches(isDisplayed()));
    }
}
