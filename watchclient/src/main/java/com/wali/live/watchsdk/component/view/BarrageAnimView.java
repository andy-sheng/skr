package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.wali.live.common.barrage.view.utils.CommentVipUtils;
import com.wali.live.event.UserActionEvent;
import com.wali.live.watchsdk.R;

/**
 * Created by zyh on 2017/12/29.
 *
 * @module 弹幕区域上方的vip弹幕动画展示
 */
public class BarrageAnimView extends RelativeLayout implements IAnimView {
    private final String TAG = "BarrageAnimView";

    private final static int LEVEL_MARGIN_LEFT = DisplayUtils.dip2px(5.67f);
    private final static int LEVEL_MARGIN_RIGHT = DisplayUtils.dip2px(5f);
    private final static int LEVEL_MARGIN_TOP = DisplayUtils.dip2px(1f);
    private final static int LEVEL_MARGIN_BOTTOM = DisplayUtils.dip2px(1.33f);

    private final static int ANIM_DV_WIDTH = DisplayUtils.dip2px(257);
    private final static int ANIM_DV_HEIGHT = DisplayUtils.dip2px(81);
    private final static int ANIM_MARGIN_RIGHT = DisplayUtils.dip2px(10);

    //该房间是否禁止播放VIP进场特效， 0 不禁止， 1 禁止
    public static final int VIP_ENTER_ROOM_EFFECT_ALLOW = 0;
    public static final int VIP_ENTER_ROOM_EFFECT_FORBID = 1;

    public final static int EFFECT_LEVEL_1 = 1;
    public final static int EFFECT_LEVEL_2 = 2;
    public final static int EFFECT_LEVEL_3 = 3;

    private TextView mLevelTv;
    private TextView mNameTv;
    private SimpleDraweeView mAnimDv;
    private RelativeLayout mContentView;
    private RelativeLayout mAnimContainer;

    private SparseIntArray mLevelBackgroundMap = new SparseIntArray(3);
    private SparseIntArray mLevelAnimMap = new SparseIntArray(3);
    private SparseArray<int[]> mLevelRangeMap = new SparseArray<>();
    private BarrageMsg mCurBarrage;
    private Animation mEnterAnimation;
    private Animation mLeaveAnimation;
    private int mCurEffectLevel = -1;
    private boolean mJoinAnimEnable = true;
    private Runnable mRunnable;
    private BarrageControlAnimView.IAnimPlayBack mAnimPlayBack;

    public void setAnimPlayBack(BarrageControlAnimView.IAnimPlayBack animPlayBack) {
        mAnimPlayBack = animPlayBack;
    }

    public void setJoinAnimEnable(boolean joinAnimEnable) {
        mJoinAnimEnable = joinAnimEnable;
    }

    public BarrageAnimView(Context context) {
        this(context, null);
    }

    public BarrageAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarrageAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initData();
        initView();
    }

    private void initData() {
        mLevelBackgroundMap.put(EFFECT_LEVEL_1, R.drawable.vip_enter_1);
        mLevelBackgroundMap.put(EFFECT_LEVEL_2, R.drawable.vip_enter_2);
        mLevelBackgroundMap.put(EFFECT_LEVEL_3, R.drawable.vip_enter_3);

        mLevelAnimMap.put(EFFECT_LEVEL_1, R.raw.vip_come_in_1);
        mLevelAnimMap.put(EFFECT_LEVEL_2, R.raw.vip_come_in_2);
        mLevelAnimMap.put(EFFECT_LEVEL_3, R.raw.vip_come_in_3);

        mLevelRangeMap.put(EFFECT_LEVEL_1, new int[]{3, 4});
        mLevelRangeMap.put(EFFECT_LEVEL_2, new int[]{5, 6});
        mLevelRangeMap.put(EFFECT_LEVEL_3, new int[]{7, Integer.MAX_VALUE});
    }

    private void initView() {
        inflate(getContext(), R.layout.enter_live_barrage_anim_view_layout, this);
        setVisibility(View.GONE);
        mAnimContainer = $(R.id.anim_container);
        mContentView = $(R.id.content_area);
        mLevelTv = $(R.id.level_tv);
        mNameTv = $(R.id.name_tv);

        $click(this, new OnClickListener() {
            @Override
            public void onClick(View v) {
                UserActionEvent.post(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, mCurBarrage.getSender(), null);
            }
        });
    }

    private void setupView() {
        setVisibility(View.VISIBLE);
        int vipLevel = mCurBarrage.getVipLevel();
        //设置徽章
        Drawable vipIcon = null;
        try {
            @DrawableRes int drawableId = CommentVipUtils.getLevelBadgeResId(vipLevel, mCurBarrage.isVipFrozen(), false);
            vipIcon = GlobalData.app().getResources().getDrawable(drawableId);
            vipIcon.setBounds(0, 0, vipIcon.getIntrinsicWidth(), vipIcon.getIntrinsicHeight());
            mLevelTv.setBackground(vipIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLevelTv.getLayoutParams();
        params.leftMargin = LEVEL_MARGIN_LEFT;
        params.rightMargin = LEVEL_MARGIN_RIGHT;
        params.topMargin = vipLevel > 4 ? 0 : LEVEL_MARGIN_TOP;
        params.bottomMargin = vipLevel > 4 ? LEVEL_MARGIN_BOTTOM : 0;
        mLevelTv.setLayoutParams(params);

        String name = TextUtils.isEmpty(mCurBarrage.getSenderName()) ?
                String.valueOf(mCurBarrage.getSender()) :
                mCurBarrage.getSenderName();
        if (name.length() > 10) {
            name = name.substring(0, 10) + "...";
        }
        mNameTv.setText(name);
        mCurEffectLevel = getLevelAnimIndex();
        if (mCurEffectLevel != -1) {
            mContentView.setBackgroundResource(mLevelBackgroundMap.get(mCurEffectLevel));
        }
    }

    private void setupLayoutParams() {
        MyLog.d(TAG, "setupLayoutParams");
        if (mAnimDv == null) {
            mAnimDv = new SimpleDraweeView(getContext());
            LayoutParams lp = new LayoutParams(ANIM_DV_WIDTH, ANIM_DV_HEIGHT);
            lp.rightMargin = ANIM_MARGIN_RIGHT;
            lp.addRule(ALIGN_RIGHT, R.id.content_area);
            mAnimContainer.addView(mAnimDv, lp);
        }
    }

    private int getLevelAnimIndex() {
        int vipLevel = mCurBarrage.getVipLevel();
        int size = mLevelRangeMap.size();
        for (int i = 1; i <= size; i++) {
            int[] list = mLevelRangeMap.get(i);
            if (vipLevel >= list[0] && vipLevel <= list[1]) {
                return i;
            }
        }
        return -1;
    }

    public void play() {
        setupView();
        setupLayoutParams();
        startEnterAnim();
    }

    private void startEnterAnim() {
        mContentView.clearAnimation();
        if (mEnterAnimation == null) {
            mEnterAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.vip_enter_room_effect_part_1);
            mEnterAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    MyLog.d(TAG, "onAnimationStart");
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    MyLog.d(TAG, "onAnimationEnd");
                    if (mCurEffectLevel != -1) {
                        FrescoWorker.frescoShowWebp(mAnimDv, mLevelAnimMap.get(mCurEffectLevel),
                                DisplayUtils.dip2px(200), DisplayUtils.dip2px(81));
                    }
                    if (mRunnable == null) {
                        mRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if (mAnimDv == null) {
                                    return;
                                }
                                mAnimDv.setVisibility(View.INVISIBLE);
                                mAnimContainer.removeView(mAnimDv);
                                mAnimDv = null;
                                startLeaveAnim();
                            }
                        };
                    }
                    postDelayed(mRunnable, 4_000);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        mContentView.startAnimation(mEnterAnimation);
    }

    private void startLeaveAnim() {
        mContentView.clearAnimation();
        if (mLeaveAnimation == null) {
            mLeaveAnimation = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.vip_enter_room_effect_part_3);
            mLeaveAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    MyLog.d(TAG, "startLeaveAnim onAnimationStart");
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    MyLog.d(TAG, "startLeaveAnim onAnimationEnd");
                    setVisibility(GONE);
                    //通知上层該弹幕消息入场动画结束了
                    mAnimPlayBack.onPlayEnd(mCurBarrage);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        mContentView.startAnimation(mLeaveAnimation);
    }

    private final <V extends View> V $(@IdRes int resId) {
        return (V) findViewById(resId);
    }

    private final <V extends View> void $click(V view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public int[] getAcceptType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_JOIN
        };
    }

    @Override
    public boolean isAccepted(BarrageMsg barrageMsg) {
        mCurBarrage = barrageMsg;
        if (!isAcceptedType() || mCurBarrage == null) {
            return false;
        }
        mCurEffectLevel = getLevelAnimIndex();
        if (mCurEffectLevel == -1 || !mJoinAnimEnable) {
            return false;
        }
        BarrageMsg.MsgExt ext = mCurBarrage.getMsgExt();
        if (ext instanceof BarrageMsg.JoinRoomMsgExt
                && !((BarrageMsg.JoinRoomMsgExt) ext).showVipEnterRoomEffect) {
            return false;
        }
        if (mCurBarrage.isVipHide() || mCurBarrage.isVipFrozen()) {
            return false;
        }
        return true;
    }

    private boolean isAcceptedType() {
        int[] types = getAcceptType();
        for (int type : types) {
            if (type == mCurBarrage.getMsgType()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onStart() {
        MyLog.d(TAG, "onStart");
        play();
        return true;
    }

    @Override
    public boolean onEnd() {
        MyLog.d(TAG, "onEnd");
        return false;
    }

    @Override
    public void reset() {
        MyLog.d(TAG, "reset");
        mAnimContainer.removeView(mAnimDv);
        Animation animation = mContentView.getAnimation();
        if (animation != null) {
            animation.cancel();
        }
        mContentView.clearAnimation();
        removeCallbacks(mRunnable);
        setVisibility(View.GONE);
        mAnimDv = null;
    }

    @Override
    public void onDestroy() {
        MyLog.d(TAG, "onDestroy");
        reset();
    }
}
