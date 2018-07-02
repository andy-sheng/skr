package com.wali.live.watchsdk.vip.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.user.User;
import com.wali.live.common.barrage.view.utils.NobleConfigUtils;
import com.wali.live.event.UserActionEvent;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.vip.contact.EnterLiveBarrageAnimContact;
import com.wali.live.watchsdk.vip.manager.OperationAnimManager;
import com.wali.live.watchsdk.vip.model.AnimationConfig;
import com.wali.live.watchsdk.vip.model.OperationAnimation;
import com.wali.live.watchsdk.vip.presenter.EnterLiveBarrageAnimPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by anping on 16-7-28.
 * 高等级用户进入房间的动画弹幕<br>
 * 业务需求
 * <ol>
 * <li>用户进入提醒一次之后，一定时间内不再提醒</li>
 * <li>不同等级不同的动画</li>
 * <li>排队　播放</li>
 * <li>权限控制，雷总房间不显示</li>
 * <p>
 * 这个view增加了播放运营活动的活动获奖用户入场动画，活动动画优先级 > 高等级用户
 * </ol>
 */
public class EnterLiveBarrageAnimView extends RelativeLayout implements ISuperLevelView{
    private final static int ACCEPT_BARRAGE_MSG_TYPE = BarrageMsgType.B_MSG_TYPE_JOIN;
    private final static String TAG = EnterLiveBarrageAnimView.class.getSimpleName() + "TAG";
    private final static String VIP_ICON_NAME = "live_vip_";

    //ui
    RelativeLayout mAniaContainer;
    TextView mLevelTv;
    TextView mNameTv;
    TextView mEnterRoomTv;
    RelativeLayout mContentArea;
    RelativeLayout operateIcon;//运营进场动画 下边的icon
    SimpleDraweeView mCoverAnima;

    //data
    private int operationAnimId;//是否播放运营活动期间相关用户的入场动画id
    private BarrageMsg mCurrentPlayBarrage; //当前正在播放的弹幕数据,通常为null;
    private SparseIntArray mLevelBackgroundMap = new SparseIntArray(3);
    private volatile long mCurrentAnchorId; // 当前的主播id
    private volatile boolean mForbidVipEnterRoomEffect = true;
    private volatile boolean mVipEnterRoomSwitchNotified = false;
    private volatile BarrageMsg mUnconfirmedSelfEnterRoomMsg;
    private int mAnimResOrder;//Vip特效资源序号
    private Set<Animation> mAnimations = Collections.synchronizedSet(new HashSet<Animation>());
    private Set<Subscription> mSubscriptions = Collections.synchronizedSet(new HashSet<Subscription>());

    private Handler mHander;
    private IPlayEndCallBack mIPlayEndCallBack;

    //presenter
    private EnterLiveBarrageAnimPresenter mPresenter;

    public EnterLiveBarrageAnimView(Context context) {
        this(context, null);
    }

    public EnterLiveBarrageAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EnterLiveBarrageAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context context) {
        inflate(context, R.layout.enter_live_anim_view_layout, this);
        mHander = new Handler();
        mAniaContainer = (RelativeLayout) this.findViewById(R.id.anima_container);
        mLevelTv = (TextView) this.findViewById(R.id.level_tv);
        mNameTv = (TextView) this.findViewById(R.id.name_tv);
        mEnterRoomTv = (TextView) this.findViewById(R.id.notify_content_tv);
        mContentArea = (RelativeLayout) this.findViewById(R.id.content_area);
        operateIcon = (RelativeLayout) this.findViewById(R.id.operate_icon);

        initPresenter();

        mLevelBackgroundMap.put(AnimationConfig.EFFECT_LEVEL_1, R.drawable.vip_enter_1);
        mLevelBackgroundMap.put(AnimationConfig.EFFECT_LEVEL_2, R.drawable.vip_enter_2);
        mLevelBackgroundMap.put(AnimationConfig.EFFECT_LEVEL_3, R.drawable.vip_enter_3);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentPlayBarrage != null) {
                    EventBus.getDefault().post(new UserActionEvent(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, mCurrentPlayBarrage.getSender(), null));
                }
            }
        });
    }

    private void initData() {
        MyLog.w(TAG, "initData");
        mLevelTv.setText("");
        mNameTv.setText("");
        mNameTv.setTextColor(getResources().getColor(R.color.color_white));
        mEnterRoomTv.setTextColor(getResources().getColor(R.color.color_ffd05e));
        mEnterRoomTv.setText(R.string.high_level_brrage_content_enterlive);

        if (mCurrentPlayBarrage != null) {
            int vipLevel = mCurrentPlayBarrage.getVipLevel();
            String name = TextUtils.isEmpty(mCurrentPlayBarrage.getSenderName()) ?
                    String.valueOf(mCurrentPlayBarrage.getSender()) :
                    mCurrentPlayBarrage.getSenderName();
            if (name.length() > 10) {
                name = name.substring(0, 10) + "...";
            }
            mNameTv.setText(name);
            if (operationAnimId > 0) { //走运营活动的配置
                MyLog.w(TAG, "operationAnimId > 0 : " + operationAnimId);
                mPresenter.getExistedAnimRes(operationAnimId);
                return;
            }

            //设置徽章
            String rName = VIP_ICON_NAME + vipLevel;
            Drawable vipIcon = null;
            try {
                @DrawableRes int drawableId = (Integer) R.drawable.class.getField(rName).get(null);
                vipIcon = GlobalData.app().getResources().getDrawable(drawableId);
                vipIcon.setBounds(0, 0, vipIcon.getIntrinsicWidth(), vipIcon.getIntrinsicHeight());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            operateIcon.setVisibility(GONE);
            mLevelTv.setVisibility(VISIBLE);
            //mLevelTv.setCompoundDrawables(vipIcon, null, null, null);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLevelTv.getLayoutParams();
            int left = getResources().getDimensionPixelSize(R.dimen.margin_17);
            int top;
            int right = getResources().getDimensionPixelOffset(R.dimen.margin_15);
            int bottom;
            if (vipLevel > 4) {
                top = 0;
                bottom = getResources().getDimensionPixelOffset(R.dimen.margin_4);
            } else {
                top = getResources().getDimensionPixelOffset(R.dimen.margin_3);
                bottom = 0;
            }
            params.setMargins(left, top, right, bottom);
            mLevelTv.setLayoutParams(params);
            mLevelTv.setBackground(vipIcon);
            resetBackgroundByLevel(vipLevel);
        }
    }

    public void initPresenter() {
        mPresenter = new EnterLiveBarrageAnimPresenter(new EnterLiveBarrageAnimContact.Iview() {
            @Override
            public void onNoRes() {
                MyLog.w(TAG, "animation == null,operationAnimId :" + operationAnimId);
                mIPlayEndCallBack.onNoRes();
            }

            @Override
            public void getExistedAnimResSuccess(OperationAnimation animation) {
                if (animation.getNobelType() > 0) {
                    setNobelIconBackground(animation);
                } else {
                    setIconAndBackground(animation);
                }
            }

            @Override
            public void transformFileToDrawableSuccess(List<Drawable> drawables) {
                if (drawables != null && !drawables.isEmpty()) {
                    Drawable drawable = drawables.get(0);
                    if(drawable != null) {
                        mContentArea.setBackground(drawable);
                    }


                    if(1 < drawables.size()) {
                        Drawable drawable1 = drawables.get(1);
                        if(drawable1 != null) {
                            operateIcon.setBackground(drawable1);
                        }
                    }
                } else {
                    mContentArea.setBackgroundResource(R.drawable.guizu_back);
                }
            }

            @Override
            public void updateVipEnterRoomEffectSwitchEvent(long anchorId, boolean enableEffect) {
                if (mCurrentAnchorId != 0 && anchorId != mCurrentAnchorId) {
                    return;
                }
                mForbidVipEnterRoomEffect = !enableEffect;
                mVipEnterRoomSwitchNotified = true;
                if (enableEffect && mUnconfirmedSelfEnterRoomMsg != null) {
                    EventBus.getDefault().post(EventClass.AddBarrageEvent.newInstance(mUnconfirmedSelfEnterRoomMsg));
                }
                mUnconfirmedSelfEnterRoomMsg = null;
            }
        });
    }

    //运营活动的进场弹幕背景
    private void setIconAndBackground(OperationAnimation animation) {
        operateIcon.setVisibility(VISIBLE);
        mLevelTv.setVisibility(GONE);

        ArrayList<String> list = new ArrayList<>();
        list.add(animation.getBottomBackPath());
        list.add(animation.getBottomIconPath());
        mPresenter.transformFileToDrawable(list);
    }

    /**
     * 设置主播id ,这个必须要设置，英文涉及到　某些主播id黑名单问题
     *
     * @param anchorId
     */
    public void setAnchorId(long anchorId) {
        MyLog.w(TAG, "setAnchorId anchorId=" + anchorId);
        mCurrentAnchorId = anchorId;
        mForbidVipEnterRoomEffect = true;
        mVipEnterRoomSwitchNotified = false;
        mUnconfirmedSelfEnterRoomMsg = null;
        this.setVisibility(GONE);
        abortAnim();

    }

    private void abortAnim() {
        MyLog.w(TAG, "abortAnim");
        mAniaContainer.removeView(mCoverAnima);
        mCoverAnima = null;
        if (null != mHander) {
            mHander.removeCallbacksAndMessages(null);
        }
        clearAllSubscriptions();
        clearAnimations();
    }

    private BaseActivity getRxActivity() {
        return (BaseActivity) getContext();
    }


    private void onPlayAnimationEnd(Animation animation) {
        MyLog.d(TAG, "translate Frame Anim play end");
        int vipLevel = mCurrentPlayBarrage.getVipLevel();
        int resId;
        if (vipLevel > 2) {
            switch (mAnimResOrder) {
                case AnimationConfig.EFFECT_LEVEL_1:
                    resId = R.raw.vip_come_in_1;
                    break;
                case AnimationConfig.EFFECT_LEVEL_2:
                    resId = R.raw.vip_come_in_2;
                    break;
                case AnimationConfig.EFFECT_LEVEL_3:
                    resId = R.raw.vip_come_in_3;
                    break;
                default:
                    MyLog.w(TAG, "unexpected anima effect level: " + mAnimResOrder);
                    resId = R.raw.vip_come_in_1;
                    break;
            }
            Uri uri = new Uri.Builder().scheme(UriUtil.LOCAL_RESOURCE_SCHEME).path(String.valueOf(resId)).build();
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(mCoverAnima.getController())
                    .setImageRequest(request)
                    .setAutoPlayAnimations(true)
                    .build();
            mCoverAnima.setController(controller);
        }

        int duration = 4000;
        Subscription subscription = Observable.timer(duration, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        MyLog.w(TAG, "onPlayAnimationEnd hidemCoverAnima");
                        mCoverAnima.setVisibility(INVISIBLE);
                        mAniaContainer.removeView(mCoverAnima);
                        mCoverAnima = null;
                        Animation animation2 = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.vip_enter_room_effect_part_3);

                        mHander.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MyLog.d(TAG, "onPlayAnimationEnd postDelayed");
                                EnterLiveBarrageAnimView.this.setVisibility(GONE);
                                mIPlayEndCallBack.endPlay(mCurrentPlayBarrage);
                                clearAllSubscriptions();
                                clearAnimations();
                            }
                        }, 300);


                        mAnimations.add(animation2);
                        MyLog.w(TAG, "onPlayAnimationEnd,mAnimations.add animation2=" + animation2.hashCode());
                        mContentArea.startAnimation(animation2);
                        MyLog.d(TAG, "play cover Anim");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(throwable);
                    }
                });
        mSubscriptions.add(subscription);
    }

    @Override
    public void play() {
        MyLog.d(TAG, "play enter Anim operationAnimId=" + operationAnimId);
        if (operationAnimId > 0) {
            playOperationAnim();
            return;
        }
        this.setVisibility(VISIBLE);
        operateIcon.setVisibility(GONE);
        if (null == mCoverAnima) {
            mCoverAnima = new SimpleDraweeView(getContext());
        }
        LayoutParams lp = new LayoutParams(DisplayUtils.dip2px(257), DisplayUtils.dip2px(81));
        lp.rightMargin = DisplayUtils.dip2px(10);
        lp.addRule(ALIGN_RIGHT, R.id.content_area);
        mAniaContainer.addView(mCoverAnima, lp);
        Animation animation1 = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.vip_enter_room_effect_part_1);
        mAnimations.add(animation1);
        MyLog.w(TAG, "play,mAnimations.add animation1=" + animation1.hashCode());

        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onPlayAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mContentArea.startAnimation(animation1);
    }

    @Override
    public boolean acceptBarrage(BarrageMsg barrageMsg) {
        if (barrageMsg.getSender() == UserAccountManager.getInstance().getUuidAsLong()) {
            MyLog.w(TAG, "acceptBarrage barrageMsg=" + barrageMsg.toString());
        }
        if (barrageMsg.getMsgType() != ACCEPT_BARRAGE_MSG_TYPE) {
            return false;
        }
        if (mCurrentAnchorId > 0 && barrageMsg.getAnchorId() != mCurrentAnchorId) {
            return false;
        }

        operationAnimId = barrageMsg.getFromEffectId();
        if (operationAnimId > 0) {
            if (barrageMsg.getNobleLevel() >= User.NOBLE_LEVEL_FOURTH && barrageMsg.isVipHide()) {
                //贵族隐身
                operationAnimId = 0;
                return false;
            }
            return true;
        }
        // 服务器还没通知，但收到自己的进入房间信息
        if (!mVipEnterRoomSwitchNotified
                && barrageMsg.getSender() == UserAccountManager.getInstance().getUuidAsLong()) {
            mUnconfirmedSelfEnterRoomMsg = barrageMsg;
            return false;
        }

        if (mForbidVipEnterRoomEffect) {
            return false;
        }

        // 服务端下发的、是否显示VIP进场特效的信息
        if (barrageMsg.getMsgExt() instanceof BarrageMsg.JoinRoomMsgExt) {
            BarrageMsg.JoinRoomMsgExt ext = (BarrageMsg.JoinRoomMsgExt) barrageMsg.getMsgExt();
            if (!ext.showVipEnterRoomEffect) {
                return false;
            }
        }

        // VIP被冻结 | VIP隐身
        if (barrageMsg.isVipFrozen() || barrageMsg.isVipHide()) {
            return false;
        }
        if (!acceptLevel(barrageMsg.getVipLevel())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onStart(BarrageMsg barrageMsg) {
        if (acceptBarrage(barrageMsg)) {
            mCurrentPlayBarrage = barrageMsg;
            initData();
            play();
            return true;
        }
        return false;
    }

    @Override
    public boolean onEnd(BarrageMsg barrageMsg) {
        return false;
    }

    /**
     * 清除所有动画
     */
    private void clearAnimations() {
        MyLog.w(TAG, "clearAnimations");
        if (null != mAnimations && mAnimations.size() > 0) {
            for (Animation animation : mAnimations) {
                animation.cancel();
                animation.reset();
                MyLog.w(TAG, "cancel animation=" + animation.hashCode());
                animation.setAnimationListener(null);
            }
            mAnimations.clear();
        }
    }

    private void resetBackgroundByLevel(int vipLevel) {
        if (mIPlayEndCallBack != null) {
            SparseArray<int[]> levelRange = mIPlayEndCallBack.getAnim(AnimationConfig.TYPE_ANIME_ENTER_ROOM).levelRange;
            int length = levelRange.size();
            for (int i = 0; i < length; i++) {
                int effectLevel = levelRange.keyAt(i);
                int[] item = levelRange.valueAt(i);
                if (vipLevel >= item[0] && vipLevel <= item[1]) {
                    if (mLevelBackgroundMap.get(effectLevel) > 0) {
                        mContentArea.setBackgroundResource(mLevelBackgroundMap.get(effectLevel));
                        mAnimResOrder = effectLevel;
                    }
                    break;
                }
            }
        }
    }

    private boolean acceptLevel(int level) {
        if (mIPlayEndCallBack != null) {
            SparseArray<int[]> levelRange = mIPlayEndCallBack.getAnim(AnimationConfig.TYPE_ANIME_ENTER_ROOM).levelRange;
            int length = levelRange.size();
            for (int i = 0; i < length; i++) {
                int[] item = levelRange.valueAt(i);
                if (level >= item[0] && level <= item[1]) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setFatherViewCallBack(IPlayEndCallBack playEndCallBack) {
        mIPlayEndCallBack = playEndCallBack;
    }

    @Override
    public void onDestroy() {
        mUnconfirmedSelfEnterRoomMsg = null;
        abortAnim();
        mHander.removeCallbacksAndMessages(null);
        mPresenter.destroy();
    }

    private class MyAnimationListener implements Animation.AnimationListener {
        long viewCode;
        OperationAnimation animat;

        MyAnimationListener(long viewCode, OperationAnimation animat) {
            this.viewCode = viewCode;
            this.animat = animat;
        }

        @Override
        public void onAnimationStart(final Animation animation) {
        }

        @Override
        public void onAnimationEnd(final Animation animation) {
            if (mCoverAnima.hashCode() != viewCode) {
                return;
            }
            MyLog.d(TAG, "translate Frame Anim play end");
            if (animat != null) {
                MyLog.d(TAG, "animat resource already exist: " + animat.getBottomWebpPath());
                Uri uri = new Uri.Builder().scheme("file").appendPath(animat.getBottomWebpPath()).build();
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                        .build();
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(mCoverAnima.getController())
                        .setImageRequest(request)
                        .setAutoPlayAnimations(true)
                        .build();
                mCoverAnima.setController(controller);
            } else {
                MyLog.d(TAG, "animat resource doesnt exist: ");
            }

            int duration = 4000;

            Subscription subscribe = Observable.timer(duration, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            //mCoverAnima.setBackground(null);
                            //bgDrawable = null;
                            if (mCoverAnima.hashCode() != viewCode) {
                                return;
                            } else {
                                MyLog.w(TAG, "playOperationAnim postDelayed the same viewCode:" + viewCode);
                            }
                            MyLog.w(TAG, "hidemCoverAnima");
                            mCoverAnima.setVisibility(INVISIBLE);
                            mAniaContainer.removeView(mCoverAnima);
                            mCoverAnima = null;
                            Animation animation2 = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.vip_enter_room_effect_part_3);
                            mHander.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MyLog.d(TAG, "playOperationAnim postDelayed");
                                    EnterLiveBarrageAnimView.this.setVisibility(GONE);
                                    clearAllSubscriptions();
                                    clearAnimations();
                                    mIPlayEndCallBack.endPlay(mCurrentPlayBarrage);
                                }
                            }, 300);

                            mAnimations.add(animation2);

                            MyLog.w(TAG, "playOperationAnimation,mAnimations.add animation2=" + animation2.hashCode());
                            mContentArea.startAnimation(animation2);
                            MyLog.d(TAG, "play cover Anim");
                        }
                    });
            mSubscriptions.add(subscribe);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private void playOperationAnimation(final OperationAnimation animat) {
        MyLog.d(TAG, "playOperationAnimation animation=" + animat.toString());
        EnterLiveBarrageAnimView.this.setVisibility(VISIBLE);
        if (null == mCoverAnima) {
            mCoverAnima = new SimpleDraweeView(getContext());
        }
        LayoutParams lp = new LayoutParams(DisplayUtils.dip2px(257f), DisplayUtils.dip2px(81f));
        lp.rightMargin = DisplayUtils.dip2px(10);
        lp.addRule(ALIGN_RIGHT, R.id.content_area);
        mAniaContainer.addView(mCoverAnima, lp);

        final Animation animation1 = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.vip_enter_room_effect_part_1);
        mAnimations.add(animation1);
        MyLog.w(TAG, "playOperationAnimation,mAnimations.add animation1=" + animation1.hashCode());
        MyAnimationListener listener = new MyAnimationListener(mCoverAnima.hashCode(), animat);
        animation1.setAnimationListener(listener);

        mContentArea.startAnimation(animation1);
    }

    //todo yss 这里播放运营活动期间的相关用户入场动画
    public void playOperationAnim() {
        MyLog.d(TAG, "playOperationAnim  enter Anim");
        setVisibility(INVISIBLE);
        Subscription subscription = Observable.create(new Observable.OnSubscribe<OperationAnimation>() {
            @Override
            public void call(Subscriber<? super OperationAnimation> subscriber) {
                OperationAnimation animat = OperationAnimManager.getExistedAnimRes(operationAnimId);
                if (animat == null) {
                    subscriber.onError(new Throwable("anime == null"));
                } else {
                    subscriber.onNext(animat);
                    subscriber.onCompleted();
                }
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(getRxActivity().<OperationAnimation>bindUntilEvent())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<OperationAnimation>() {
                    @Override
                    public void call(final OperationAnimation animat) {
                        playOperationAnimation(animat);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.d(TAG, "animation resource doesnt exist , go and download res");
                        mPresenter.tryLoadAnimRes();
                        EnterLiveBarrageAnimView.this.setVisibility(View.GONE);
                    }
                });
        mSubscriptions.add(subscription);
    }

    private void clearAllSubscriptions() {
        MyLog.w(TAG, "clearAllSubscriptions");
        if (null != mSubscriptions && mSubscriptions.size() > 0) {
            for (Subscription subscription : mSubscriptions) {
                subscription.unsubscribe();
                MyLog.w(TAG, "clearAllSubscriptions subscription=" + subscription.hashCode());
            }
            mSubscriptions.clear();
        }
    }

    private void setNobelIconBackground(OperationAnimation animation) {
        operateIcon.setVisibility(GONE);
        mLevelTv.setVisibility(VISIBLE);
        mNameTv.setTextColor(getResources().getColor(R.color.color_FFEA4C));
        mEnterRoomTv.setTextColor(getResources().getColor(R.color.color_white));
        switch (animation.getNobelType()) {
            case User.NOBLE_LEVEL_TOP:
                mEnterRoomTv.setText(animation.getBottomText());
                break;
            case User.NOBLE_LEVEL_SECOND:
                mEnterRoomTv.setText(animation.getBottomText());
                break;
            case User.NOBLE_LEVEL_THIRD:
                mEnterRoomTv.setText(animation.getBottomText());
                break;
            case User.NOBLE_LEVEL_FOURTH:
                mEnterRoomTv.setText(R.string.nobel_bo_enter);
                break;
            case User.NOBLE_LEVEL_FIFTH:
                mEnterRoomTv.setText(R.string.nobel_zi_enter);
                break;
            default:
                mEnterRoomTv.setText(R.string.high_level_brrage_content_enterlive);
                break;
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLevelTv.getLayoutParams();
        int left = getResources().getDimensionPixelSize(R.dimen.margin_17);
        int top = 0;
        int right = getResources().getDimensionPixelOffset(R.dimen.margin_15);
        int bottom = getResources().getDimensionPixelOffset(R.dimen.margin_4);
        params.setMargins(left, top, right, bottom);
        mLevelTv.setLayoutParams(params);
        mLevelTv.setBackgroundResource(NobleConfigUtils.getImageResoucesByNobelLevelInBarrage(animation.getNobelType()));

        ArrayList<String> list = new ArrayList<>();
        list.add(animation.getBottomBackPath());
        mPresenter.transformFileToDrawable(list);
    }

}
