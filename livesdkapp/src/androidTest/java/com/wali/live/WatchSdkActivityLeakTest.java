package com.wali.live;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.mi.liveassistant.R;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import org.junit.Rule;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.mi.live.data.api.LiveManager.TYPE_LIVE_PUBLIC;
import static com.wali.live.watchsdk.base.BaseComponentSdkActivity.EXTRA_ROOM_INFO;

/**
 * Created by yangli on 2017/11/4.
 */
@RunWith(AndroidJUnit4.class)
public class WatchSdkActivityLeakTest extends BaseActivityLeakTest {

    @Rule
    public final ActivityTestRule targetActivityTestRule =
            new ActivityTestRule<>(WatchSdkActivity.class, false, false);

    @Override
    protected final String getTAG() {
        return "WatchSdkActivityLeakTest";
    }

    private Intent createIntent() {
        final long playerId = 3136168;
        RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, null, null)
                .setLiveType(TYPE_LIVE_PUBLIC)
                .setEnableShare(true).build();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ROOM_INFO, roomInfo);
        return intent;
    }

    @Override
    protected void onPerformStartActivity() {
        targetActivityTestRule.launchActivity(createIntent());
    }

    @Override
    protected void onPerformActivityTest() throws InterruptedException {
        for (int i = 0; i < 5; ++i) {
            onView(withId(R.id.msg_ctrl_btn)).perform(click());
            waitForInteraction();
            uiDevice.pressBack();
            waitForInteraction();
            onView(withId(R.id.gift_btn)).perform(click());
            waitForInteraction();
            uiDevice.pressBack();
            waitForInteraction();
        }
    }

    @Override
    protected void onPerformFinishActivity() {
        targetActivityTestRule.finishActivity();
    }
}
