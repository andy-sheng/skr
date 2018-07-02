package com.wali.live.watchsdk.vip.view;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.vip.contact.NobleUserEnterBigAnimContact;
import com.wali.live.watchsdk.vip.model.OperationAnimation;
import com.wali.live.watchsdk.vip.presenter.NobleUserEnterBigAnimPresenter;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import rx.Subscription;

/**
 * 贵族进场大动画
 */
public class NobleUserEnterBigAnimView extends RelativeLayout implements ISuperLevelView {
    private final static String TAG = "NobleUserEnterBigAnimView";
    private final static int ACCEPT_BARRAGE_MSG_TYPE = BarrageMsgType.B_MSG_TYPE_JOIN;
    private final static int SHOW_ANIM_LEVEL_6 = 6;
    private final static int SHOW_ANIM_LEVEL_7 = 7;

    //data
    private volatile boolean mForbidVipEnterRoomEffect = true;//是否禁止播放vip进场特效
    private volatile boolean mVipEnterRoomSwitchNotified = false;//是否收到服务器明确通知是否启用/禁用VIP入场特效
    private volatile BarrageMsg mUnconfirmedSelfEnterRoomMsg;// 服务器还没通知，但收到自己的进入房间信息
    private volatile long mCurrentAnchorId;// 当前的主播id
    private BarrageMsg mCurrentPlayBarrage; //当前正在播放的弹幕数据,通常为null;
    private long mSenderId;//当前入场UserID
    private String mNickName;//当前入场的昵称
    private int mVipLevel;//当前入场的等级
    private int operationAnimId;//运营活动期间相关用户的入场动画id
    private Set<Subscription> mSubscriptions = Collections.synchronizedSet(new HashSet<Subscription>());
    protected RoomBaseDataModel mMyRoomData;

    //ui
    private SimpleDraweeView mBigAnimSv;
    private BaseImageView mAvatarIv;
    private BaseImageView mNobelImageView;// 贵族入场动画
    private View mViewContainer;
    private TextView mNickNameTv;
    private TextView mLevelHitTv;
    private ImageView mLevelIconIv;

    private IPlayEndCallBack mIplayEndCallBack;
    private Handler mHandler = new Handler();

    //presenter
    private NobleUserEnterBigAnimPresenter mPresenter;

    public NobleUserEnterBigAnimView(Context context) {
        super(context);
        init(context);
    }

    public NobleUserEnterBigAnimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NobleUserEnterBigAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.noble_user_enter_big_anim, this);
        mBigAnimSv = (SimpleDraweeView) findViewById(R.id.big_anim_view);
        mAvatarIv = (BaseImageView) findViewById(R.id.vip_anim_avatar_iv);
        mNobelImageView = (BaseImageView) findViewById(R.id.nobel_image);
        mViewContainer = findViewById(R.id.container_view);
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
        mLevelHitTv = (TextView) findViewById(R.id.level_hit_tv);
        mLevelIconIv = (ImageView) findViewById(R.id.level_iv);

        initPresenter();
    }

    private void initPresenter() {
        mPresenter = new NobleUserEnterBigAnimPresenter(new NobleUserEnterBigAnimContact.IView() {
            @Override
            public void updateVipEnterRoomEffectSwitchEvent(long anchorId, boolean enableEffect) {
                if (mCurrentAnchorId != 0 && anchorId != mCurrentAnchorId) {
                    return;
                }
                mForbidVipEnterRoomEffect = !enableEffect;
                mVipEnterRoomSwitchNotified = true;
                mUnconfirmedSelfEnterRoomMsg = null;
            }

            @Override
            public void getExistedAnimResSuccess(OperationAnimation animation) {
                int duration = animation == null ? 3000 : animation.getEffectDuration();
                int nobelLevel = animation.getNobelType();
                updateBottomViewByOperation(nobelLevel, animation.isRound(), animation.isNeedTopUserInfo());
                MyLog.d(TAG, " resource already exist: " + animation.getTopWebpPath() + ",duration=" + duration + " nobelLevel: "+ nobelLevel);

                Uri uri = new Uri.Builder().scheme("file").appendPath(animation.getTopWebpPath()).build();
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri).build();

                DraweeController controller;
                if (nobelLevel <= 0) {
                    controller = Fresco.newDraweeControllerBuilder()
                            .setOldController(mBigAnimSv.getController())
                            .setImageRequest(request)
                            .setControllerListener(generateControllerListenerByOperation(nobelLevel, duration))
                            .build();
                    mBigAnimSv.setController(controller);
                    mBigAnimSv.setVisibility(View.VISIBLE);
                } else {
                    controller = Fresco.newDraweeControllerBuilder()
                            .setOldController(mNobelImageView.getController())
                            .setImageRequest(request)
                            .setAutoPlayAnimations(true)
                            .build();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            abortAnim();
                        }
                    }, duration);
                    mNobelImageView.setController(controller);
                    mNobelImageView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void getExistedAnimResFail() {
                mPresenter.loadAnimRes();
                abortAnim();
            }
        });
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

    @Override
    public void play() {
        //这里播放VIP动画（头部动画）
        if (mMyRoomData != null) {
            MyLog.d(TAG, "play is in radio room");
            return;
        }

        this.setVisibility(VISIBLE);
        MyLog.d(TAG, "play enter Anim");
        if (operationAnimId > 0) {
            playOperationAnim();
            return;
        }
        mAvatarIv.setVisibility(GONE);
        mViewContainer.setVisibility(INVISIBLE);
        mNickNameTv.setVisibility(GONE);
        mLevelHitTv.setVisibility(GONE);
        mLevelIconIv.setVisibility(GONE);
        mNickNameTv.setText(mNickName);
        mLevelHitTv.setText(R.string.vip_enter_hit);
        AvatarUtils.loadHexagonAvatarByUrl(mAvatarIv, AvatarUtils.getAvatarUrlByUid(mSenderId, AvatarUtils.SIZE_TYPE_AVATAR_SMALL), R.drawable.avatar_default_vip);

        mBigAnimSv.setVisibility(VISIBLE);
        ControllerListener controllerListener = new BaseControllerListener() {
            @Override
            public void onFinalImageSet(String id, Object imageInfo, Animatable animatable) {
                animatable.start();

                mAvatarIv.setVisibility(VISIBLE);
                Animation avatarScaleAnimation = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.anim_scale_in_avatar_vip);
                mAvatarIv.startAnimation(avatarScaleAnimation);

                //先来200毫秒背景渐显
                mViewContainer.setVisibility(VISIBLE);
                Animation containerFadeInAnimation = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.anim_fade_in_vip_enter_name);
                mViewContainer.startAnimation(containerFadeInAnimation);

                //播放200毫秒之后来300毫秒动画显示昵称
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNickNameTv.setVisibility(VISIBLE);
                        Animation nickNameScaleInAnimation = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.anim_scale_in_vip_enter_name);
                        mNickNameTv.startAnimation(nickNameScaleInAnimation);
                    }
                }, 200);

                //500毫秒之后 用300毫秒显示大咖驾到四个字
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLevelHitTv.setVisibility(VISIBLE);
                        mLevelIconIv.setVisibility(VISIBLE);
                        Animation levelHitScaleInAnimation = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.anim_scale_in_vip_enter_name);
                        mLevelHitTv.startAnimation(levelHitScaleInAnimation);
                        mLevelIconIv.startAnimation(levelHitScaleInAnimation);
                    }
                }, 500);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        abortAnim();
                    }
                }, 3000);
            }
        };
        Uri uri;
        if (mVipLevel == SHOW_ANIM_LEVEL_6) {
            uri = new Uri.Builder().scheme("res").path(String.valueOf(R.drawable.special_vip_come_in_6)).build();
            mLevelIconIv.setImageResource(R.drawable.viplevel6);
        } else {
            uri = new Uri.Builder().scheme("res").path(String.valueOf(R.drawable.special_vip_come_in_7)).build();
            mLevelIconIv.setImageResource(R.drawable.viplevel7);
        }
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(mBigAnimSv.getController())
                .setImageRequest(request)
                .setControllerListener(controllerListener)
                .build();
        mBigAnimSv.setController(controller);
    }

    @Override
    public boolean acceptBarrage(BarrageMsg barrageMsg) {
        MyLog.d(TAG, "acceptBarrage barrageMsg=" + barrageMsg.toString());

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
            mSenderId = barrageMsg.getSender();
            mNickName = barrageMsg.getSenderName();
            return true;
        }

//         TODO: 17-5-12 just for VIP test
//        if (Constants.isDebugOrTestBuild) {
//            barrageMsg.setVipLevel(3 + (int) (System.currentTimeMillis() % 5));
//            barrageMsg.setVipHide(false);
//            barrageMsg.setVipFrozen(false);
//            MyLog.d(TAG, barrageMsg.toString());
//            MyLog.d(TAG, AvatarUtils.getAvatarUrlByUid(barrageMsg.getSender(), AvatarUtils.SIZE_TYPE_AVATAR_SMALL));
//            mSenderId = barrageMsg.getSender();
//            mNickName = barrageMsg.getSenderName();
//            mVipLevel = barrageMsg.getVipLevel();
//            return true;
//        }

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

        mSenderId = barrageMsg.getSender();
        mNickName = barrageMsg.getSenderName();
        mVipLevel = barrageMsg.getVipLevel();
        return true;
    }

    @Override
    public boolean onStart(BarrageMsg barrageMsg) {
        if (acceptBarrage(barrageMsg)) {
            MyLog.d(TAG, "onStart");
            mCurrentPlayBarrage = barrageMsg;
            resetData();
            play();
            return true;
        }
        return false;
    }

    @Override
    public boolean onEnd(BarrageMsg barrageMsg) {
        MyLog.d(TAG, "onEnd");
        return false;
    }

    @Override
    public void setAnchorId(long anchorId) {
        mCurrentAnchorId = anchorId;
        mForbidVipEnterRoomEffect = true;
        mVipEnterRoomSwitchNotified = false;
        mUnconfirmedSelfEnterRoomMsg = null;
        abortAnim();
    }

    @Override
    public void setFatherViewCallBack(IPlayEndCallBack playEndCallBack) {
        mIplayEndCallBack = playEndCallBack;
    }

    private void resetData() {

    }

    private boolean acceptLevel(int level) {
        return level == SHOW_ANIM_LEVEL_6 || level == SHOW_ANIM_LEVEL_7;
    }

    private void abortAnim() {
        DraweeController controller = mBigAnimSv.getController();
        if (controller != null) {
            Animatable animation = controller.getAnimatable();
            if (animation != null) {
                animation.stop();
            }
        }
        mBigAnimSv.setVisibility(GONE);
        mNobelImageView.setVisibility(GONE);
        this.setVisibility(GONE);
        if (null != mIplayEndCallBack) {
            mIplayEndCallBack.getUiHandle().post(new Runnable() {
                @Override
                public void run() {
                    if (mIplayEndCallBack != null) {
                        mIplayEndCallBack.endPlay(mCurrentPlayBarrage);
                        mCurrentPlayBarrage = null;
                    }
                }
            });
        }
        clearAllSubscriptions();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        mUnconfirmedSelfEnterRoomMsg = null;
        mHandler.removeCallbacksAndMessages(null);
        abortAnim();

        mPresenter.destroy();
    }

    private void updateBottomViewByOperation(int nobelLevel, boolean isRound, boolean isSHowUser) {
        int vis = isSHowUser ? VISIBLE : GONE;
        if (isSHowUser) {
            mViewContainer.setVisibility(VISIBLE);
        } else {
            mViewContainer.setVisibility(INVISIBLE);
        }
        if (nobelLevel <= 0) {
            mAvatarIv.setVisibility(vis);
            mNickNameTv.setVisibility(vis);
            mLevelHitTv.setVisibility(vis);
            mLevelIconIv.setVisibility(GONE);
            mNickNameTv.setText(mNickName);
            mLevelHitTv.setText(R.string.vip_enter_hit);
            if (isRound) {
                AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.getAvatarUrlByUid(mSenderId, AvatarUtils.SIZE_TYPE_AVATAR_SMALL), true);
            } else {
                AvatarUtils.loadHexagonAvatarByUrl(mAvatarIv, AvatarUtils.getAvatarUrlByUid(mSenderId, AvatarUtils.SIZE_TYPE_AVATAR_SMALL), R.drawable.avatar_default_vip);
            }
            mBigAnimSv.setVisibility(VISIBLE);
        } else {
            mAvatarIv.setVisibility(vis);
            mLevelIconIv.setVisibility(GONE);
        }
    }

    private ControllerListener generateControllerListenerByOperation(int nobelLevel, final int duration) {
        return new BaseControllerListener() {
            @Override
            public void onFinalImageSet(String id, Object imageInfo, Animatable animatable) {
                animatable.start();
                mAvatarIv.setVisibility(VISIBLE);
                Animation avatarScaleAnimation = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.anim_scale_in_avatar_vip);
                mAvatarIv.startAnimation(avatarScaleAnimation);

                //先来200毫秒背景渐显
                mViewContainer.setVisibility(VISIBLE);
                Animation containerFadeInAnimation = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.anim_fade_in_vip_enter_name);
                mViewContainer.startAnimation(containerFadeInAnimation);

                //播放200毫秒之后来300毫秒动画显示昵称
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNickNameTv.setVisibility(VISIBLE);
                        Animation nickNameScaleInAnimation = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.anim_scale_in_vip_enter_name);
                        mNickNameTv.startAnimation(nickNameScaleInAnimation);
                    }
                }, 200);

                //500毫秒之后 用300毫秒显示大咖驾到四个字
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLevelHitTv.setVisibility(VISIBLE);
                        Animation levelHitScaleInAnimation = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.anim_scale_in_vip_enter_name);
                        mLevelHitTv.startAnimation(levelHitScaleInAnimation);
                    }
                }, 500);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        abortAnim();
                    }
                }, duration);
            }
        };
    }

    public void setRoomData(RoomBaseDataModel myRoomData) {
        mMyRoomData = myRoomData;
    }

    // 这里播放运营活动期间的相关用户入场动画
    public void playOperationAnim() {
        //这里播放贵族动画（头部动画）
        if (mMyRoomData != null) {
            MyLog.d(TAG, "the room is radio room");
            return;
        }

        this.setVisibility(VISIBLE);
        MyLog.d(TAG, "playOperationAnim operation action enter Anim");

        mPresenter.getExistedAnimRes(operationAnimId);
    }

    private Drawable transformFileToDrawable(String path) {
        Drawable drawable = null;
        File tempFile = new File(path);
        if (tempFile.exists()) {
            try {
                drawable = BitmapDrawable.createFromPath(tempFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (drawable == null) {
            MyLog.d("BitmapToDrawable", "Fail to transform drawable");
        }
        return drawable;
    }
}
