package com.module.playways.grab.room.top;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabPlaySeatUpdateEvent;
import com.module.playways.grab.room.event.LightOffAnimationOverEvent;
import com.module.playways.grab.room.fragment.GrabRoomFragment;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.MLightInfoModel;
import com.module.playways.grab.room.model.WantSingerInfo;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GrabPlayerRv2 extends RelativeLayout {
    public final static String TAG = "GrabPlayerRv2";
    public final static int PLAYER_COUNT = 7;
    private LinkedHashMap<Integer, VP> mInfoMap = new LinkedHashMap<>();
    private ArrayList<VP> mGrabTopItemViewArrayList = new ArrayList<>(PLAYER_COUNT);
    private GrabRoomData mRoomData;
    AnimatorSet mAnimatorAllSet;

    LinearLayout mContentLl;
    SVGAParser mSVGAParser;

    int mCurSeq = -2;

    volatile boolean mHasBurst = false;


    public GrabPlayerRv2(Context context) {
        super(context);
        init();
    }

    public GrabPlayerRv2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabPlayerRv2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_top_content_view_layout, this);
        mContentLl = (LinearLayout) this.findViewById(R.id.content_ll);
        addChildView();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void addChildView() {
        for (int i = 0; i < PLAYER_COUNT; i++) {
            VP vp = new VP();
            vp.grabTopItemView = new GrabTopItemView(getContext());
            if (i == PLAYER_COUNT - 1) {
                vp.grabTopItemView.setCanShowInviteWhenEmpty(true);
            } else {
                vp.grabTopItemView.setCanShowInviteWhenEmpty(false);
            }
            vp.grabTopItemView.setGrap(false);
            vp.grabTopItemView.tryAddParent(mContentLl);
            vp.grabTopItemView.setToPlaceHolder();
            vp.SVGAImageView = new SVGAImageView(getContext());
            LayoutParams lp = new LayoutParams(U.getDisplayUtils().dip2px(100), U.getDisplayUtils().dip2px(100));
            GrabPlayerRv2.this.addView(vp.SVGAImageView, lp);
            mGrabTopItemViewArrayList.add(vp);
        }
        resetAllGrabTopItemView();
    }

    private void resetAllGrabTopItemView() {
        for (VP vp : mGrabTopItemViewArrayList) {
            vp.grabTopItemView.reset();
            vp.grabTopItemView.setToPlaceHolder();
        }
    }

    //只有轮次切换的时候调用
    private void initData() {
        if (!mRoomData.hasGameBegin()) {
            MyLog.d(TAG, "游戏未开始，不能用轮次信息里更新头像");
            resetAllGrabTopItemView();
            List<GrabPlayerInfoModel> list = mRoomData.getPlayerInfoList();
            for (int i = 0; i < list.size() && i < mGrabTopItemViewArrayList.size(); i++) {
                VP vp = mGrabTopItemViewArrayList.get(i);
                GrabPlayerInfoModel playerInfoModel = list.get(i);
                mInfoMap.put(playerInfoModel.getUserID(), vp);
                vp.grabTopItemView.bindData(playerInfoModel, mRoomData.getOwnerId() == playerInfoModel.getUserID());
            }
        } else {
            GrabRoundInfoModel now = mRoomData.getExpectRoundInfo();
            if (now == null) {
                MyLog.w(TAG, "initData data error");
                return;
            }
            if (mCurSeq == now.getRoundSeq()) {
                MyLog.w(TAG, "initdata 轮次一样，无需更新");
                return;
            }
            mCurSeq = now.getRoundSeq();
            for (int i = 0; i < mGrabTopItemViewArrayList.size(); i++) {
                VP vp = mGrabTopItemViewArrayList.get(i);
                vp.grabTopItemView.setVisibility(VISIBLE);
            }
            resetAllGrabTopItemView();
            List<GrabPlayerInfoModel> playerInfoModels = now.getPlayUsers();
            mInfoMap.clear();
            MyLog.d(TAG, "initData playerInfoModels.size() is " + playerInfoModels.size());
            for (int i = 0; i < playerInfoModels.size() && i < mGrabTopItemViewArrayList.size(); i++) {
                VP vp = mGrabTopItemViewArrayList.get(i);
                GrabPlayerInfoModel playerInfoModel = playerInfoModels.get(i);
                mInfoMap.put(playerInfoModel.getUserID(), vp);
                vp.grabTopItemView.bindData(playerInfoModel, mRoomData.getOwnerId() == playerInfoModel.getUserID());
                if(playerInfoModels.get(i).getUserID() == now.getUserID()
                        && now.getStatus() == GrabRoundInfoModel.STATUS_SING){
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vp.grabTopItemView.getLayoutParams();
                    lp.weight = 0;
                    vp.grabTopItemView.setLayoutParams(lp);
                }
            }

            MyLog.d(TAG, "initData + now.getStatus() " + now.getStatus());
            if (now.getStatus() == GrabRoundInfoModel.STATUS_GRAB) {
                for (WantSingerInfo wantSingerInfo : now.getWantSingInfos()) {
                    VP vp = mInfoMap.get(wantSingerInfo.getUserID());
                    if (vp != null && vp.grabTopItemView != null) {
                        vp.grabTopItemView.setGrap(true);
                    }
                }
            } else {
                MyLog.d(TAG, "initData else");
                for (VP vp : mGrabTopItemViewArrayList) {
                    if (vp != null && vp.grabTopItemView != null) {
                        MyLog.d(TAG, "initData else 2");
                        vp.grabTopItemView.setGrap(false);
                    }
                }
            }
        }
        RelativeLayout.LayoutParams lp = (LayoutParams) mContentLl.getLayoutParams();
        lp.leftMargin = U.getDisplayUtils().dip2px(15);
        lp.rightMargin = U.getDisplayUtils().dip2px(15);
        mContentLl.setLayoutParams(lp);
    }

    //这里可能人员有变动
    public void setModeGrab() {
        // 切换到抢唱模式
        if (mAnimatorAllSet != null) {
            mAnimatorAllSet.cancel();
        }
        mHasBurst = false;
        initData();
        for (int uId : mInfoMap.keySet()) {
            VP vp = mInfoMap.get(uId);
            if (vp != null && vp.grabTopItemView != null) {
                vp.grabTopItemView.setVisibility(VISIBLE);
                vp.grabTopItemView.reset();
                WantSingerInfo wantSingerInfo = new WantSingerInfo();
                wantSingerInfo.setUserID(uId);
                GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
                // TODO: 2019/2/26 判空
                if (grabRoundInfoModel != null && grabRoundInfoModel.getWantSingInfos().contains(wantSingerInfo)) {
                    vp.grabTopItemView.setGrap(true);
                } else {
//                    if (vp.grabTopItemView.getPlayerInfoModel().isOnline()) {
//                        vp.grabTopItemView.setGrap(false);
//                    } else {
//                        //离线了
//                    }
                    vp.grabTopItemView.setGrap(false);
                }
            }
        }

        RelativeLayout.LayoutParams lp = (LayoutParams) mContentLl.getLayoutParams();
        lp.leftMargin = U.getDisplayUtils().dip2px(15);
        lp.rightMargin = U.getDisplayUtils().dip2px(15);
        mContentLl.setLayoutParams(lp);
    }

    public void setModeSing(int singUid) {
        MyLog.d(TAG, "setModeSing" + " singUid=" + singUid);
        VP vp = mInfoMap.get(singUid);
        if (vp == null) {
            MyLog.d(TAG, "没有在选手席位找到 id=" + singUid + " 相应ui，return");
            return;
        }
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now == null) {
            return;
        }
        if (!now.isParticipant() && now.getEnterStatus() == GrabRoundInfoModel.STATUS_SING) {
            // 如果是演唱阶段进来的参与者
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vp.grabTopItemView.getLayoutParams();
            lp.weight = 0;
            vp.grabTopItemView.setLayoutParams(lp);
            syncLight();
            EventBus.getDefault().post(new LightOffAnimationOverEvent());
            return;
        }

        if (vp.grabTopItemView != null) {
            vp.grabTopItemView.setGetSingChance();
        }

        List<Animator> allAnimator = new ArrayList<>();
        GrabTopItemView finalGrabTopItemView = vp.grabTopItemView;

        {
            // 这是圈圈动画
            ValueAnimator objectAnimator1 = new ValueAnimator();
            objectAnimator1.setIntValues(0, 100);
            objectAnimator1.setDuration(495);
            objectAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int p = (int) animation.getAnimatedValue();
                    finalGrabTopItemView.mCircleAnimationView.setProgress(p);
                }
            });
            objectAnimator1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    finalGrabTopItemView.mCircleAnimationView.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    finalGrabTopItemView.mCircleAnimationView.setVisibility(GONE);
                }
            });

            // 接下来是头像放大一点的动画
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(vp.grabTopItemView.mAvatarIv, View.SCALE_X, 1, 1.08f);
            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(vp.grabTopItemView.mAvatarIv, View.SCALE_Y, 1, 1.08f);
            AnimatorSet animatorSet23 = new AnimatorSet();
            animatorSet23.playTogether(objectAnimator2, objectAnimator3);
            animatorSet23.setDuration(4 * 33);

            AnimatorSet animatorSet123 = new AnimatorSet();
            animatorSet123.playTogether(objectAnimator1, animatorSet23);
            allAnimator.add(animatorSet123);
        }
        // 等待47个节拍
        {
            // 放大透明度消失
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(vp.grabTopItemView, View.ALPHA, 1, 0);
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(vp.grabTopItemView.mAvatarIv, View.SCALE_X, 1.0f, 1.08f);
            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(vp.grabTopItemView.mAvatarIv, View.SCALE_Y, 1.0f, 1.08f);

            ValueAnimator objectAnimator4 = new ValueAnimator();
            objectAnimator4.setFloatValues(1, 0);
            objectAnimator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float weight = (float) animation.getAnimatedValue();
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) finalGrabTopItemView.getLayoutParams();
                    lp.weight = weight;
                    finalGrabTopItemView.setLayoutParams(lp);

                    RelativeLayout.LayoutParams lp2 = (LayoutParams) mContentLl.getLayoutParams();
                    int t = (int) (U.getDisplayUtils().dip2px(15) * weight);
                    lp2.leftMargin = U.getDisplayUtils().dip2px(30) - t;
                    lp2.rightMargin = U.getDisplayUtils().dip2px(30) - t;
                    mContentLl.setLayoutParams(lp2);
                }
            });
            AnimatorSet animatorSet1234 = new AnimatorSet();
            animatorSet1234.playTogether(objectAnimator1, objectAnimator2, objectAnimator3, objectAnimator4);
            animatorSet1234.setDuration(9 * 33);
            animatorSet1234.setStartDelay(47 * 33);
            allAnimator.add(animatorSet1234);
        }

//        {
//            // 耳机的出现
//            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mErjiIv, View.TRANSLATION_Y, -U.getDisplayUtils().dip2px(100), 0);
//            objectAnimator1.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//                    super.onAnimationStart(animation);
//                    mErjiIv.setVisibility(VISIBLE);
//                }
//            });
//            objectAnimator1.setDuration(14 * 33);
//
//            List<Animator> mieDengList = new ArrayList<>();
//            mieDengList.add(objectAnimator1);
//            int i = 0;
//            // 灯的出现，以灭灯的形式出现
//            for (int uId : mInfoMap.keySet()) {
//                if (uId == singUid) {
//                    continue;
//                }
//                VP vp1 = mInfoMap.get(uId);
//                GrabTopItemView itemView = vp1.grabTopItemView;
//                if (!itemView.getPlayerInfoModel().isOnline()) {
//                    continue;
//                }
//                ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(itemView.mFlagIv, View.SCALE_X, 1, 2);
//                ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(itemView.mFlagIv, View.SCALE_Y, 1, 2);
//                ObjectAnimator objectAnimator4 = ObjectAnimator.ofFloat(itemView.mFlagIv, View.ALPHA, 0, 1);
//                ObjectAnimator objectAnimator5 = ObjectAnimator.ofFloat(itemView.mFlagIv, View.TRANSLATION_Y, U.getDisplayUtils().dip2px(100), 0);
//                AnimatorSet animatorSet2345 = new AnimatorSet();
//                animatorSet2345.playTogether(objectAnimator2, objectAnimator3, objectAnimator4, objectAnimator5);
//                animatorSet2345.setDuration(7 * 33);
//
//                ObjectAnimator objectAnimator6 = ObjectAnimator.ofFloat(itemView.mFlagIv, View.SCALE_X, 2, 1);
//                ObjectAnimator objectAnimator7 = ObjectAnimator.ofFloat(itemView.mFlagIv, View.SCALE_Y, 2, 1);
//                AnimatorSet animatorSet67 = new AnimatorSet();
//                animatorSet67.playTogether(objectAnimator6, objectAnimator7);
//                animatorSet67.setDuration(3 * 33);
//                AnimatorSet animatorSet234567 = new AnimatorSet();
//                animatorSet234567.playSequentially(animatorSet2345, animatorSet67);
//                animatorSet234567.setStartDelay(i * 4 * 33);
//                animatorSet234567.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//                        super.onAnimationStart(animation);
//                        itemView.setLight(false);
//                    }
//                });
//                mieDengList.add(animatorSet234567);
//                i++;
//            }
//            AnimatorSet animatorSet1_234567s = new AnimatorSet();
//            animatorSet1_234567s.playTogether(mieDengList);
//            allAnimator.add(animatorSet1_234567s);
//        }
        // 等 125 个节拍
        {
            List<Animator> liangdengList = new ArrayList<>();
            int i = 0;
            for (int uId : mInfoMap.keySet()) {
                if (uId == singUid) {
                    continue;
                }

                VP vp1 = mInfoMap.get(uId);
                GrabTopItemView itemView = vp1.grabTopItemView;
//                if (!itemView.getPlayerInfoModel().isOnline()) {
//                    continue;
//                }
                ValueAnimator objectAnimator1 = new ValueAnimator();
                objectAnimator1.setIntValues(0, 0);
                objectAnimator1.setDuration(1);
                final int tti = i;
                objectAnimator1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        if (tti == 0) {
                            U.getSoundUtils().play(GrabRoomFragment.TAG, R.raw.grab_lightup);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        onAnimationEnd(animation);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        itemView.setLight(true);
                    }
                });
                objectAnimator1.setStartDelay(i * 4 * 33);
                i++;
                liangdengList.add(objectAnimator1);
            }
            AnimatorSet animatorSet1s = new AnimatorSet();
            animatorSet1s.playTogether(liangdengList);
            animatorSet1s.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                }
            });
            animatorSet1s.setStartDelay(20 * 33);
            allAnimator.add(animatorSet1s);
        }

        if (mAnimatorAllSet != null) {
            mAnimatorAllSet.cancel();
        }

        mAnimatorAllSet = new AnimatorSet();
        mAnimatorAllSet.playSequentially(allAnimator);
        mAnimatorAllSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                //setModeGrab();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                syncLight();
                EventBus.getDefault().post(new LightOffAnimationOverEvent());
            }
        });
        mAnimatorAllSet.start();
    }

    //有人爆灯了，这个时候所有的灯都闪烁
    public void toBurstState() {
        mHasBurst = true;
        for (int uId : mInfoMap.keySet()) {
            VP vp = mInfoMap.get(uId);
            if (vp != null && vp.grabTopItemView != null) {
                vp.grabTopItemView.startEvasive();
            }
        }
    }

    private void syncLight() {
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            for (MLightInfoModel noPassingInfo : now.getMLightInfos()) {
                VP vp = mInfoMap.get(noPassingInfo.getUserID());
                if (vp != null && vp.grabTopItemView != null) {
                    vp.grabTopItemView.setLight(false);
                }
            }
        }
    }

    public void grap(int uid) {
        VP vp = mInfoMap.get(uid);
        if (vp != null && vp.grabTopItemView != null) {
            vp.grabTopItemView.setGrap(true);
        }
    }

    public void lightOff(int uid) {
        if (mHasBurst) {
            MyLog.w(TAG, "已经爆灯了，所以灭灯也忽略 uid 是：" + uid);
            return;
        }
        VP vp = mInfoMap.get(uid);
        if (vp != null && vp.grabTopItemView != null) {
//            setLightOffAnimation(vp);
            setLightOff(vp);
        }
    }

    public void onlineChange(PlayerInfoModel playerInfoModel) {
        if (playerInfoModel != null && playerInfoModel.getUserInfo() != null) {
            VP vp = mInfoMap.get(playerInfoModel.getUserInfo().getUserId());
            if (vp != null) {
                vp.grabTopItemView.updateOnLineState(playerInfoModel);
            }
        } else {
            MyLog.w(TAG, "onlineChange playerInfoModel error");
        }
    }

    private void setLightOff(VP vp) {
        GrabTopItemView grabTopItemView = vp.grabTopItemView;
        grabTopItemView.setLight(false);
    }

    /**
     * 执行灭灯动画
     *
     * @param vp
     */
    private void setLightOffAnimation(VP vp) {
        GrabTopItemView grabTopItemView = vp.grabTopItemView;
        grabTopItemView.mFlagIv.setVisibility(GONE);

        int[] position1 = new int[2];
        grabTopItemView.mFlagIv.getLocationInWindow(position1);

        int[] position2 = new int[2];
        SVGAImageView mMieDengIv = vp.SVGAImageView;
        mMieDengIv.getLocationInWindow(position2);

        mMieDengIv.setTranslationX(position1[0] - U.getDisplayUtils().dip2px(32));
        mMieDengIv.setTranslationY(U.getDisplayUtils().dip2px(12f));

        getSVGAParser().parse("grab_miedeng.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                mMieDengIv.setVisibility(VISIBLE);
                mMieDengIv.stopAnimation(true);
                mMieDengIv.setImageDrawable(drawable);
                mMieDengIv.startAnimation();
            }

            @Override
            public void onError() {

            }
        });

        mMieDengIv.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                mMieDengIv.stopAnimation(true);
                mMieDengIv.setVisibility(GONE);
                grabTopItemView.setLight(false);
            }

            @Override
            public void onRepeat() {
                onFinished();
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    private SVGAParser getSVGAParser() {
        if (mSVGAParser == null) {
            mSVGAParser = new SVGAParser(U.app());
            mSVGAParser.setFileDownloader(new SVGAParser.FileDownloader() {
                @Override
                public void resume(final URL url, final Function1<? super InputStream, Unit> complete, final Function1<? super Exception, Unit> failure) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder().url(url).get().build();
                            try {
                                Response response = client.newCall(request).execute();
                                complete.invoke(response.body().byteStream());
                            } catch (IOException e) {
                                e.printStackTrace();
                                failure.invoke(e);
                            }
                        }
                    }).start();
                }
            });
        }
        return mSVGAParser;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabPlaySeatUpdateEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        initData();
    }

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
        if (mGrabTopItemViewArrayList.size() != 0) {
            VP vp = mGrabTopItemViewArrayList.get(mGrabTopItemViewArrayList.size() - 1);
            if (mRoomData.isOwner()) {
                vp.grabTopItemView.setCanShowInviteWhenEmpty(true);
            } else {
                vp.grabTopItemView.setCanShowInviteWhenEmpty(false);
            }
        }
        initData();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimatorAllSet != null) {
            mAnimatorAllSet.cancel();
        }
        for (int i = 0; i < mGrabTopItemViewArrayList.size(); i++) {
            VP vp = mGrabTopItemViewArrayList.get(i);
            if (vp != null) {
                if (vp.SVGAImageView != null) {
                    vp.SVGAImageView.setCallback(null);
                    vp.SVGAImageView.stopAnimation(true);
                }
            }
        }
        EventBus.getDefault().unregister(this);
    }

    static class VP {
        GrabTopItemView grabTopItemView;
        SVGAImageView SVGAImageView;
    }
}