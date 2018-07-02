package com.wali.live.watchsdk.vip.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.RelativeLayout;

import com.base.activity.BaseActivity;
import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.gift.utils.AnimationPlayControlTemplate;
import com.wali.live.watchsdk.vip.contact.NobleUserEnterAnimControlContact;
import com.wali.live.watchsdk.vip.model.AnimationConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 贵族进场动画
 */

public class NobleUserEnterAnimControlView extends RelativeLayout implements NobleUserEnterAnimControlContact.IView {
    private static final String TAG = NobleUserEnterAnimControlView.class.getSimpleName();

    private volatile long mCurrentAnchorId; // 当前的主播id
    private boolean mCloseView;
    private AnimationPlayControlTemplate<BarrageMsg> mFlyBarrageControl; //播放队列控制器
    private List<ISuperLevelView> mSuperLevelViews = new ArrayList<>();
    protected RoomBaseDataModel mMyRoomData;
    private SparseArray<AnimationConfig> mAnimationConfig = new SparseArray<>(2);

    private Handler mHandle = new Handler();
    private volatile Handler mSelfEnterRoomDelayHandler;//当前线程通过MessageQueue发消息，不能粗暴地清空MessageQueue
    private volatile Runnable mSelfEnterRoomRunnable;

    public NobleUserEnterAnimControlView(Context context) {
        this(context, null);
    }

    public NobleUserEnterAnimControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NobleUserEnterAnimControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context context) {
        mFlyBarrageControl = new AnimationPlayControlTemplate<BarrageMsg>((BaseActivity) getContext(), false, 1) {
            @Override
            public void onStart(BarrageMsg cur) {
                MyLog.d(TAG, " onStart ");
                if (mSuperLevelViews != null && cur != null) {
                    for (ISuperLevelView item : mSuperLevelViews) {
                        if (item.acceptBarrage(cur)) {
                            item.onStart(cur);
                            break;
                        }
                    }
                }
            }

            @Override
            protected void onEnd(BarrageMsg model) {
                MyLog.d(TAG, " onEnd ");

                if (mSuperLevelViews != null && model != null) {
                    for (ISuperLevelView item : mSuperLevelViews) {
                        if (item.acceptBarrage(model)) {
                            item.onEnd(model);
                            break;
                        }
                    }
                }
            }
        };

        NobleUserEnterBigAnimView vip6EnterLiveAnimView = new NobleUserEnterBigAnimView(context);
        vip6EnterLiveAnimView.setFatherViewCallBack(mPlayEndCallBack);
        vip6EnterLiveAnimView.setVisibility(GONE);
        mSuperLevelViews.add(vip6EnterLiveAnimView);
        addView(vip6EnterLiveAnimView);
    }

    public void setRoomData(RoomBaseDataModel myRoomData){
        mMyRoomData = myRoomData;

        for (ISuperLevelView item : mSuperLevelViews) {
            if (item instanceof NobleUserEnterBigAnimView) {
                NobleUserEnterBigAnimView vip6EnterLiveAnimView = (NobleUserEnterBigAnimView)(item);
                vip6EnterLiveAnimView.setRoomData(mMyRoomData);
                break;
            }
        }
    }

    /**
     * 如果是自己的进场消息，为了防止展示进场特效时其他View还没初始化好，这里延迟1秒
     */
    private boolean handleSelfEnterRoom(final BarrageMsg barrage) {
        if (barrage.getMsgType() == BarrageMsgType.B_MSG_TYPE_JOIN
                && barrage.getSender() == UserAccountManager.getInstance().getUuidAsLong()) {
            // 在同一个线程执行add操作，对mFlyBarrageControl里的非volatile变量进行线程封闭
            if (mSelfEnterRoomDelayHandler == null) {
                if (Looper.myLooper() == null) {
                    return false;
                }
                mSelfEnterRoomDelayHandler = new Handler(Looper.myLooper());
            }
            mSelfEnterRoomRunnable = new Runnable() {
                @Override
                public void run() {
                    mFlyBarrageControl.add(barrage, true);
                }
            };
            mSelfEnterRoomDelayHandler.postDelayed(mSelfEnterRoomRunnable, 1_500);
            return true;
        }
        return false;
    }

    private IPlayEndCallBack mPlayEndCallBack = new IPlayEndCallBack() {
        @Override
        public void endPlay(BarrageMsg barrageMsg) {
            if (mFlyBarrageControl != null) {
                mFlyBarrageControl.endCurrent(barrageMsg);
            }
        }

        @Override
        public AnimationConfig getAnim(int type) {
            return mAnimationConfig.get(type);
        }

        @Override
        public Handler getUiHandle() {
            return mHandle;
        }

        @Override
        public void onNoRes() {
            if (mFlyBarrageControl != null) {
                mFlyBarrageControl.endCurrent(null);
            }
        }
    };

    @Override
    public void putBarrage(BarrageMsg enterLiveBarrage) {
        if (enterLiveBarrage != null && !mCloseView) {
            if (mSuperLevelViews != null) {
                for (ISuperLevelView item : mSuperLevelViews) {
                    if (item.acceptBarrage(enterLiveBarrage)) {
                        if (handleSelfEnterRoom(enterLiveBarrage)) {
                            break;
                        }
                        mFlyBarrageControl.add(enterLiveBarrage, true);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void setAnchorId(long anchorId) {
        mCurrentAnchorId = anchorId;
        if (mSuperLevelViews != null) {
            for (ISuperLevelView item : mSuperLevelViews) {
                item.setAnchorId(anchorId);
            }
        }
        mFlyBarrageControl.reset();
        mHandle.removeCallbacksAndMessages(null);
        if (mSelfEnterRoomDelayHandler != null && mSelfEnterRoomRunnable != null) {
            mSelfEnterRoomDelayHandler.removeCallbacks(mSelfEnterRoomRunnable);
        }
    }

    @Override
    public void destory() {
        if (mFlyBarrageControl != null) {
            mFlyBarrageControl.destroy();
        }
        mHandle.removeCallbacksAndMessages(null);
        if (mSuperLevelViews != null) {
            for (ISuperLevelView item : mSuperLevelViews) {
                item.onDestroy();
            }
        }
        if (mSelfEnterRoomDelayHandler != null) {
            if (mSelfEnterRoomRunnable != null) {
                mSelfEnterRoomDelayHandler.removeCallbacks(mSelfEnterRoomRunnable);
            }
            mSelfEnterRoomDelayHandler = null;// 这里不能停它的Looper
        }
    }
}

