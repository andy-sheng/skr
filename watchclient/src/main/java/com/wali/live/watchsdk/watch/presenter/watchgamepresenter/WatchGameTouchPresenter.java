package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.thornbirds.component.view.IOrientationListener;
import com.wali.live.proto.LiveMessageProto;
import com.wali.live.watchsdk.eventbus.EventClass;

import org.greenrobot.eventbus.EventBus;

import static com.wali.live.watchsdk.eventbus.EventClass.WatchGameControllChangeEvent.WATCH_GAME_CONTROLL_BRIGHTNESS;
import static com.wali.live.watchsdk.eventbus.EventClass.WatchGameControllChangeEvent.WATCH_GAME_CONTROLL_DEFAULT;
import static com.wali.live.watchsdk.eventbus.EventClass.WatchGameControllChangeEvent.WATCH_GAME_CONTROLL_VOLUME;

public class WatchGameTouchPresenter extends ComponentPresenter implements View.OnTouchListener,
        IOrientationListener {

    private static final String TAG = "WatchGameTouchPresenter";

    View touchView;

    private float startX;
    private float startY;

    private int startType; //记录开始时调节的类型

    private boolean isClick;
    private float distanceX;
    private float distanceY;

    private float brightness; //记录开始调节时亮度
    private int currVolume; //记录开始调节时音量

    public WatchGameTouchPresenter(@NonNull IEventController controller, @NonNull View touchView) {
        super(controller);
        this.touchView = touchView;
        this.touchView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();

                if (startX < DisplayUtils.getScreenWidth() / 2) {
                    startType = WATCH_GAME_CONTROLL_BRIGHTNESS;
                } else {
                    startType = WATCH_GAME_CONTROLL_VOLUME;
                }
                brightness = Math.abs(CommonUtils.getScreenBrightness(touchView.getContext()));
                currVolume = CommonUtils.getStreamVolume();

                isClick = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float endX = event.getX();
                float endY = event.getY();

                distanceX = startX - endX;
                distanceY = startY - endY;

                if (endX < DisplayUtils.getScreenWidth() / 2) {
                    // 左边屏幕控制亮度
                    if (startType == WATCH_GAME_CONTROLL_BRIGHTNESS) {
                        float percent = distanceY / (float) DisplayUtils.getScreenHeight();
                        float brightnessOffset = percent / 1.5f; // 减少灵敏度

                        MyLog.d(TAG, "brightnessOffset = " + brightnessOffset);
                        if (Math.abs(brightnessOffset) >= 0.01) {
                            float setBrightness = brightness + brightnessOffset;
                            if (setBrightness < 0) {
                                setBrightness = 0;
                            } else if (setBrightness > 1) {
                                setBrightness = 1;
                            }

                            CommonUtils.setScreenBrightness(touchView.getContext(), setBrightness);
                            EventBus.getDefault().post(new EventClass.WatchGameControllChangeEvent(WATCH_GAME_CONTROLL_BRIGHTNESS, setBrightness, true));
                        }
                    }
                } else {
                    // 右边屏幕控制音量
                    if (startType == WATCH_GAME_CONTROLL_VOLUME) {
                        float percent = distanceY / (float) DisplayUtils.getScreenHeight();
                        int maxVolume = CommonUtils.getStreamMaxVolume();

                        float volumeOffsetAccurate = maxVolume * percent; // 减少灵敏度
                        int volumeOffset = (int) volumeOffsetAccurate;

                        MyLog.d(TAG, "volumeOffsetAccurate = " + volumeOffsetAccurate);
                        if (Math.abs(volumeOffset) >= 1) {
                            int setVolume = currVolume + volumeOffset;
                            if (setVolume < 0) {
                                setVolume = 0;
                            } else if (setVolume >= maxVolume) {
                                setVolume = maxVolume;
                            }

                            CommonUtils.setStreamVolume(setVolume);
                            float volumePercent = (float) setVolume / (float) maxVolume;
                            EventBus.getDefault().post(new EventClass.WatchGameControllChangeEvent(WATCH_GAME_CONTROLL_VOLUME, volumePercent, true));
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(distanceY) > 10
                        || Math.abs(distanceX) > 10) {
                    isClick = true;
                } else {
                    isClick = false;
                }
                EventBus.getDefault().post(new EventClass.WatchGameControllChangeEvent(WATCH_GAME_CONTROLL_DEFAULT, 0, false));
                break;
        }
        return isClick;
    }

    @Override
    protected String getTAG() {
        return null;
    }

    @Override
    public boolean onEvent(int i, IParams iParams) {
        return false;
    }

    @Override
    public void onOrientation(boolean b) {

    }
}
