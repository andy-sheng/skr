package com.mi.liveassistant.unity;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mi.liveassistant.common.display.DisplayUtils;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.room.manager.watch.WatchManager;
import com.mi.liveassistant.room.manager.watch.callback.IWatchCallback;
import com.mi.liveassistant.room.manager.watch.callback.IWatchListener;
import com.xiaomi.player.Player;

/**
 * Created by yangli on 2017/4/22.
 *
 * @module Unity观看直播辅助类
 */
public class WatchForUnity extends UnitySdk<Activity, IUnityWatchListener> {
    private static final String TAG = "WatchForUnity";

    private static final int DEFAULT_WIDTH = DisplayUtils.dip2px(320f);
    private static final int DEFAULT_HEIGHT = DisplayUtils.dip2px(180);

    private WatchManager mWatchManager;

    private ViewGroup mRootView;
    private ViewGroup mContainerView;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public WatchForUnity(Activity activity, IUnityWatchListener listener) {
        super(activity, listener);
        MyLog.w(TAG, "WatchForUnity");
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "WatchForUnity addContentView");
                mRootView = ((ViewGroup) mActivity.findViewById(android.R.id.content));
                mContainerView = new RelativeLayout(mActivity);
                mRootView.addView(mContainerView, new ViewGroup.LayoutParams(
                        DEFAULT_WIDTH, DEFAULT_HEIGHT));

                mWatchManager = new WatchManager(new IWatchListener() {
                    @Override
                    public void onEndUnexpected() {
                        MyLog.w(TAG, "onEndUnexpected");
                        if (mUnityListener != null) {
                            mUnityListener.onEndUnexpected();
                        }
                    }
                });
                mWatchManager.setContainerView(mContainerView);
                View view = mContainerView.getChildAt(0);
                if (view != null && view instanceof SurfaceView) {
                    ((SurfaceView) view).setZOrderMediaOverlay(true);
                }
                mWatchManager.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFit,
                        DEFAULT_WIDTH, DEFAULT_HEIGHT);

//                listView(mActivity.getWindow().getDecorView(), "");
            }
        });
    }

    private void listView(View view, String depth) {
        MyLog.e(TAG, depth + "|-" + view);
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); ++i) {
                listView(((ViewGroup) view).getChildAt(i), depth + "| ");
            }
        }
    }

    public void setViewPosition(final int x, final int y, final int width, final int height) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "setViewPosition [" + x + ", " + y + ", " + width + ", " + height + "]");
                mContainerView.setX(x);
                mContainerView.setY(y);
                ViewGroup.LayoutParams layoutParams = mContainerView.getLayoutParams();
                layoutParams.width = width;
                layoutParams.height = height;
                mContainerView.setLayoutParams(layoutParams);
            }
        });
    }

    public void startWatch(final long playerId, final @NonNull String liveId) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "startWatch playerId=" + playerId + ", liveId=" + liveId);
                mWatchManager.enterLive(playerId, liveId, new IWatchCallback() {
                    @Override
                    public void notifyFail(int errCode) {
                        MyLog.w(TAG, "enterLive failed, errCode=" + errCode);
                        if (mUnityListener != null) {
                            mUnityListener.onEnterLiveFailed(errCode);
                        }
                    }

                    @Override
                    public void notifySuccess() {
                        MyLog.w(TAG, "enterLive success");
                        if (mUnityListener != null) {
                            mUnityListener.onEnterLiveSuccess();
                        }
                    }
                });
            }
        });
    }

    public void stopWatch() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "stopWatch");
                mWatchManager.leaveLive();
            }
        });
    }

    public void destroy() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "destroy");
                mRootView.removeView(mContainerView);
            }
        });
    }
}
