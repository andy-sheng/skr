package com.wali.live.watchsdk.vip.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ResImage;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.wali.live.event.UserActionEvent;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.vip.model.AnimationConfig;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by anping on 16-7-28.
 * 高等级用户进入房间的动画弹幕
 * <p>
 * 　业务需求
 * １．用户进入提醒一次之后，十分钟内不在提醒
 * 　２．　不同等级不同的动画
 * 3. 排队　播放
 * ４．权限控制，雷总房间不显示
 */
public class LevelUpgradeBarrageAnimView extends RelativeLayout implements ISuperLevelView {


    private static final int ANIM_PLAY_TIMES = 5 * 1000; //动画播放的时间
    private final static int SMOKE_ANIM_PLAY_TIME = 3 * 1000;
    private final static int LIGHT_ANIM_PLAY_TIME = 500;


    private final static int ACCEPET_BARRAGE_MSG_TYPE = BarrageMsgType.B_MSG_TYPE_ANIM;
    private final static int ACCEPET_ANIM_EFFECT = BarrageMsg.AnimMsgExt.LEVEL_UPGREAD_ANIMATION_TYPE;


    TextView mCongratulateTv;


    SimpleDraweeView mIcon;


    TextView mNameTv;


    TextView mUpgradeTv;


    ImageView mLightIv;


    ImageView mAnimeIv;


    RelativeLayout mIconAndContentRL;


    RelativeLayout mContentArea;


    private BarrageMsg mCurrentPlayBarrage; //当前正在播放的弹幕数据,通常为null;

    private IPlayEndCallBack mIplayEndCallBack;

    //优化下
    private SparseArray<Integer> mAnimLevelIntrval = new SparseArray<>();

    private final static int LIGHT_IMG_WIDTH = DisplayUtils.dip2px(68);

    public LevelUpgradeBarrageAnimView(Context context) {
        this(context, null);
    }

    public LevelUpgradeBarrageAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LevelUpgradeBarrageAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context context) {
        inflate(context, R.layout.level_upgrade_barrage_anim_view_layout, this);
        mCongratulateTv = (TextView) this.findViewById(R.id.congratulate_tv);
        mIcon = (SimpleDraweeView) this.findViewById(R.id.icon_iv);
        mNameTv = (TextView) this.findViewById(R.id.name_tv);
        mUpgradeTv = (TextView) this.findViewById(R.id.notify_content_tv);
        mLightIv = (ImageView) this.findViewById(R.id.light_iv);
        mAnimeIv = (ImageView) this.findViewById(R.id.anime_iv);
        mIconAndContentRL = (RelativeLayout) this.findViewById(R.id.iconWithContentArea);
        mContentArea = (RelativeLayout) this.findViewById(R.id.content_area);

        mAnimLevelIntrval.put(1, R.drawable.lv_up_1);
        mAnimLevelIntrval.put(2, R.drawable.lv_up_2);
        mAnimLevelIntrval.put(3, R.drawable.lv_up_3);
        mAnimLevelIntrval.put(4, R.drawable.lv_up_3);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentPlayBarrage != null) {
                    EventBus.getDefault().post(new UserActionEvent(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, mCurrentPlayBarrage.getSender(), null));
                }
            }
        });
    }


    /**
     * 设置主播id ,这个必须要设置，英文涉及到　某些主播id黑名单问题
     *
     * @param anchorId
     */
    public void setAnchorId(long anchorId) {
        setVisibility(GONE);
    }

    @Override
    public void play() {

        this.setVisibility(VISIBLE);

        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        measure(w, h);
        final int viewSize = mContentArea.getMeasuredWidth() - LIGHT_IMG_WIDTH;

        Animation animation = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.level_upgrade_slide_left_in);
        mIconAndContentRL.startAnimation(animation);

        if (getAnime(mCurrentPlayBarrage.getSenderLevel()) == 4 && mIplayEndCallBack.getUiHandle() != null) {
            mIplayEndCallBack.getUiHandle().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLightIv.setVisibility(VISIBLE);
                    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
                    animator.setTarget(mLightIv);
                    animator.setRepeatCount(1);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            mLightIv.setTranslationX((Float) animation.getAnimatedValue() * viewSize);
                            mLightIv.setAlpha(1 - (float) Math.pow((Float) animation.getAnimatedValue(), 3));
                        }
                    });
                    animator.setDuration(LIGHT_ANIM_PLAY_TIME).start();

                    mIplayEndCallBack.getUiHandle().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LevelUpgradeBarrageAnimView.this.setVisibility(GONE);

                            if (mIplayEndCallBack != null) {
                                mIplayEndCallBack.endPlay(mCurrentPlayBarrage);
                                mCurrentPlayBarrage = null;
                            }
                        }
                    }, LIGHT_ANIM_PLAY_TIME * 4);
                }
            }, LIGHT_ANIM_PLAY_TIME * 4);
        } else {
            mIplayEndCallBack.getUiHandle().postDelayed(new Runnable() {
                @Override
                public void run() {
                    LevelUpgradeBarrageAnimView.this.setVisibility(GONE);

                    if (mIplayEndCallBack != null) {
                        mIplayEndCallBack.endPlay(mCurrentPlayBarrage);
                        mCurrentPlayBarrage = null;
                    }
                }
            }, LIGHT_ANIM_PLAY_TIME * 10);
        }
    }

    @Override
    public boolean acceptBarrage(BarrageMsg barrageMsg) {
        if (barrageMsg.getSender() == UserAccountManager.getInstance().getUuidAsLong()) {
            MyLog.w(TAG, "acceptBarrage barrageMsg=" + barrageMsg.toString());
        }
        if (barrageMsg.getMsgType() != ACCEPET_BARRAGE_MSG_TYPE) {
            return false;
        }
        BarrageMsg.AnimMsgExt animMsgExt = (BarrageMsg.AnimMsgExt) barrageMsg.getMsgExt();
        return accepctLevel(barrageMsg.getSenderLevel()) && animMsgExt != null && animMsgExt.animationType == ACCEPET_ANIM_EFFECT;
    }

    @Override
    public boolean onStart(BarrageMsg barrageMsg) {
        if (acceptBarrage(barrageMsg)) {
            mCurrentPlayBarrage = barrageMsg;
            resetData();
            play();
            return true;
        }
        return false;
    }

    @Override
    public boolean onEnd(BarrageMsg barrageMsg) {
        return false;
    }


    @SuppressLint("StringFormatMatches")
    public void resetData() {
        mNameTv.setText("");
        ResImage resImage = new ResImage(R.drawable.avatar_default_a);
        FrescoWorker.loadLocalImage(mIcon, resImage);
        mNameTv.setText("");
        mUpgradeTv.setText("");
        if (mCurrentPlayBarrage != null) {
            String name = TextUtils.isEmpty(mCurrentPlayBarrage.getSenderName()) ? String.valueOf(mCurrentPlayBarrage.getSender()) : mCurrentPlayBarrage.getSenderName();
            if (name.length() > 10) {
                name = name.substring(0, 10) + "...";
            }
            mNameTv.setText(name);
            AvatarUtils.loadAvatarByUidTs(mIcon, mCurrentPlayBarrage.getSender(), 0, true);
            String contentText = GlobalData.app().getResources().getString(R.string.high_level_brrage_level_upgrade);
            mUpgradeTv.setText(String.format(contentText, mCurrentPlayBarrage.getSenderLevel()));
            resetBackgroudByLevel(mCurrentPlayBarrage.getSenderLevel());
        }
    }

    @Override
    public void setFatherViewCallBack(IPlayEndCallBack playEndCallBack) {
        mIplayEndCallBack = playEndCallBack;
    }

    @Override
    public void onDestroy() {

    }


    private void resetBackgroudByLevel(int level) {
        if (mIplayEndCallBack != null) {
            SparseArray<int[]> levelRange = mIplayEndCallBack.getAnim(AnimationConfig.TYPE_ANIME_LEVEL_UPGRAGE).levelRange;
            int length = levelRange.size();
            for (int i = 0; i < length; i++) {
                int key = levelRange.keyAt(i);
                int[] item = levelRange.get(key);
                if (level >= item[0] && level <= item[1]) {
                    if (mAnimLevelIntrval.get(key) > 0) {
                        mContentArea.setBackgroundResource(mAnimLevelIntrval.get(key));
                    }
                    break;
                }
            }
        }
    }

    private boolean accepctLevel(int level) {
        if (mIplayEndCallBack != null) {
            SparseArray<int[]> levelRange = mIplayEndCallBack.getAnim(AnimationConfig.TYPE_ANIME_LEVEL_UPGRAGE).levelRange;
            int length = levelRange.size();
            for (int i = 0; i < length; i++) {
                int key = levelRange.keyAt(i);
                int[] item = levelRange.get(key);
                if (level >= item[0] && level <= item[1]) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getAnime(int level) {
        if (mIplayEndCallBack != null) {
            SparseArray<int[]> levelRange = mIplayEndCallBack.getAnim(AnimationConfig.TYPE_ANIME_LEVEL_UPGRAGE).levelRange;
            int length = levelRange.size();
            for (int i = 0; i < length; i++) {
                int key = levelRange.keyAt(i);
                int[] item = levelRange.get(key);
                if (level >= item[0] && level <= item[1]) {
                    return key;
                }
            }
        }
        return 0;
    }
}
