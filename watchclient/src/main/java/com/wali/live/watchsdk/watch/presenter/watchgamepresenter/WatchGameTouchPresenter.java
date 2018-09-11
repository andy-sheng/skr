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
    private long startTime = 0;
    private long endTime = 0;

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

                isClick = false;
                startTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                float endX = event.getX();
                float endY = event.getY();
                float distanceY = startY - endY;

                if (endX < DisplayUtils.getScreenWidth() / 2) {
                    // 左边屏幕控制亮度
                    if (startType == WATCH_GAME_CONTROLL_BRIGHTNESS) {
                        float percent = distanceY / (float) DisplayUtils.getScreenHeight();
                        float brightness = Math.abs(CommonUtils.getScreenBrightness(touchView.getContext()));
                        float brightnessOffset = percent / 10; // 减少灵敏度

                        if (Math.abs(brightnessOffset) >= 0.01) {
                            brightness += brightnessOffset;
                            if (brightness < 0) {
                                brightness = 0;
                            } else if (brightness > 1) {
                                brightness = 1;
                            }

                            CommonUtils.setScreenBrightness(touchView.getContext(), brightness);
                            EventBus.getDefault().post(new EventClass.WatchGameControllChangeEvent(WATCH_GAME_CONTROLL_BRIGHTNESS, brightness, true));
                        }
                    }
                } else {
                    // 右边屏幕控制音量
                    if (startType == WATCH_GAME_CONTROLL_VOLUME) {
                        float percent = distanceY / (float) DisplayUtils.getScreenHeight();
                        int currVolume = CommonUtils.getStreamVolume();
                        int maxVolume = CommonUtils.getStreamMaxVolume();

                        float volumeOffsetAccurate = maxVolume * percent / 10; // 减少灵敏度
                        int volumeOffset = (int) volumeOffsetAccurate;
                        
                        if (volumeOffset == 0 && Math.abs(volumeOffsetAccurate) > 0.2f) {
                            if (distanceY > 0) {
                                volumeOffset = 1;
                            } else if (distanceY < 0) {
                                volumeOffset = -1;
                            }
                        }

                        if (Math.abs(volumeOffset) >= 1) {
                            currVolume += volumeOffset;
                            if (currVolume < 0) {
                                currVolume = 0;
                            } else if (currVolume >= maxVolume) {
                                currVolume = maxVolume;
                            }

                            CommonUtils.setStreamVolume(currVolume);
                            float volumePercent = (float) currVolume / (float) maxVolume;
                            EventBus.getDefault().post(new EventClass.WatchGameControllChangeEvent(WATCH_GAME_CONTROLL_VOLUME, volumePercent, true));
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                endTime = System.currentTimeMillis();
                if ((endTime - startTime) > 0.1 * 1000L) {
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
