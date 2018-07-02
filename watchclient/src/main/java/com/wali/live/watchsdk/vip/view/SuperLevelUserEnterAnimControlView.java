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
import com.wali.live.common.gift.utils.AnimationPlayControlTemplate;
import com.wali.live.watchsdk.vip.contact.SuperLevelUserEnterAnimControlContact;
import com.wali.live.watchsdk.vip.model.AnimationConfig;

import java.util.ArrayList;
import java.util.List;


/**
 * 等级高的用户才能够享有的动画
 */
public class SuperLevelUserEnterAnimControlView extends RelativeLayout implements SuperLevelUserEnterAnimControlContact.IView {
    private static final String TAG = SuperLevelUserEnterAnimControlView.class.getSimpleName();

    private AnimationPlayControlTemplate<BarrageMsg> mFlyBarrageControl; //播放队列控制器

    private List<ISuperLevelView> mSuperLevelViews = new ArrayList<>();
    private SparseArray<AnimationConfig> mAnimationConfig = new SparseArray<>(2);

    private Handler mHandle = new Handler();
    private volatile Handler mSelfEnterRoomDelayHandler;//当前线程通过MessageQueue发消息，不能粗暴地清空MessageQueue
    private volatile Runnable mSelfEnterRoomRunnable;

    private volatile long mCurrentAnchorId; // 当前的主播id
    private boolean mCloseView;

    public SuperLevelUserEnterAnimControlView(Context context) {
        this(context, null);
    }

    public SuperLevelUserEnterAnimControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SuperLevelUserEnterAnimControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
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

    protected void init(Context context) {
        mFlyBarrageControl = new AnimationPlayControlTemplate<BarrageMsg>((BaseActivity) getContext(), false, 1) {
            @Override
            public void onStart(BarrageMsg cur) {
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

        mAnimationConfig.put(AnimationConfig.TYPE_ANIME_ENTER_ROOM, new AnimationConfig(AnimationConfig.TYPE_ANIME_ENTER_ROOM));
        mAnimationConfig.put(AnimationConfig.TYPE_ANIME_LEVEL_UPGRAGE, new AnimationConfig(AnimationConfig.TYPE_ANIME_LEVEL_UPGRAGE));

        EnterLiveBarrageAnimView enterLiveBarrageAnimView = new EnterLiveBarrageAnimView(context);
        LevelUpgradeBarrageAnimView levelUpgradeBarrageAnimView = new LevelUpgradeBarrageAnimView(context);

        enterLiveBarrageAnimView.setFatherViewCallBack(mPlayEndCallBack);
        levelUpgradeBarrageAnimView.setFatherViewCallBack(mPlayEndCallBack);

        enterLiveBarrageAnimView.setVisibility(GONE);
        levelUpgradeBarrageAnimView.setVisibility(GONE);

        mSuperLevelViews.add(enterLiveBarrageAnimView);
        mSuperLevelViews.add(levelUpgradeBarrageAnimView);

        addView(enterLiveBarrageAnimView);
        addView(levelUpgradeBarrageAnimView);
    }

    /**
     * 如果是自己的进场消息，为了防止展示进场特效时其他View还没初始化好，这里延迟1秒
     *
     * @param barrage
     * @return 不继续处理
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

    public static final int VIP_ENTER_ROOM_EFFECT_ENABLE_MIN_LEVEL = 3;

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
    public void destory() {
        if (mFlyBarrageControl != null) {
            mFlyBarrageControl.destroy();
        }
        mHandle.removeCallbacks(null);
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

    @Override
    public void reset() {

    }
}
