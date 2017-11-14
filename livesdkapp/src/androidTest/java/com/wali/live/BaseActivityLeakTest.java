package com.wali.live;

import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiDevice;
import android.text.TextUtils;
import android.util.Log;

import com.mi.liveassistant.R;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.wali.live.Utils.formatTime;
import static com.wali.live.Utils.joinPath;
import static com.wali.live.Utils.newExternalPath;

/**
 * Created by yangli on 2017/11/6.
 */
public abstract class BaseActivityLeakTest {
    private static final int DEFAULT_LAUNCH_COUNT = 20;

    protected final String TAG = getTAG();

    @Rule
    public final ActivityTestRule mainActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, false, false);

    protected final UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());

    protected final Context applicationContext = getInstrumentation().getTargetContext();
    private final Runtime runtime = Runtime.getRuntime();
    private final String dumpFilePath =
            newExternalPath(joinPath("LeakTest", applicationContext.getPackageName()));

    protected abstract String getTAG();

    protected final void waitForLaunchMainActivity() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(1000);
    }

    protected final void waitForLaunchActivity() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(400);
    }

    protected final void waitForFinishActivity() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(400);
    }

    protected final void waitForEnd() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(15000);
    }

    protected final void waitForInteraction() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(400);
    }

    private final void waitForGc() throws InterruptedException {
        runtime.gc();
        TimeUnit.MILLISECONDS.sleep(100);
    }

    protected void onPreTest() throws Exception {
        onView(withId(R.id.login_tv)).perform(click());
        waitForInteraction();
    }

    protected abstract void onPerformStartActivity() throws Exception;

    protected abstract void onPerformActivityTest() throws Exception;

    protected abstract void onPerformFinishActivity() throws Exception;

    protected int getLaunchCount() {
        return DEFAULT_LAUNCH_COUNT;
    }

    @Test
    public final void detectActivityLeak() throws Exception {
        Intent intent = new Intent();
        mainActivityTestRule.launchActivity(intent);
        waitForLaunchMainActivity();
        onPreTest();

        for (int i = 0, size = getLaunchCount(); i < size; ++i) {
            onPerformStartActivity();
            waitForLaunchActivity();

            onPerformActivityTest();

            onPerformFinishActivity();
            waitForFinishActivity();

            waitForGc();
            Log.w(TAG, String.format("detectActivityLeak i=%d" + " memory=%dkb", i,
                    (runtime.totalMemory() - runtime.freeMemory()) / 1024));
        }

        dumpHeapFile(dumpFilePath);

        Log.w(TAG, "detectActivityLeak wait end");
        waitForEnd();
    }

    private final void dumpHeapFile(String dumpFilePath) throws IOException {
        if (TextUtils.isEmpty(dumpFilePath)) {
            Log.e(TAG, "dumpHeapFile but dumpFilePath is empty");
            return;
        }
        Log.w(TAG, "dumpHeapFile start");
        final String dumpFileName = formatTime(System.currentTimeMillis()) + ".hprof";
        final String dumpFile = joinPath(dumpFilePath, dumpFileName);
        Debug.dumpHprofData(dumpFile);
        Log.w(TAG, "dumpHeapFile done " + dumpFileName);

        StringBuffer stringBuffer = new StringBuffer("You can use cmd:\n");
        stringBuffer.append("adb pull ").append(dumpFile).append("; ");
        stringBuffer.append("hprof-conv ").append(dumpFileName).append(" converted-dump.hprof; ");
        stringBuffer.append("rm -f ").append(dumpFileName).append(";\n");
        stringBuffer.append("to fetch and convert dumpfile, and use MAT to analyse potential leaks.");
        Log.w(TAG, stringBuffer.toString());
    }

}
