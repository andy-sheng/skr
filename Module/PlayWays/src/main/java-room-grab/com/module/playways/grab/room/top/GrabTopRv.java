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

import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.RoomData;
import com.module.playways.grab.room.event.LightOffAnimationOverEvent;
import com.module.playways.grab.room.event.ShowPersonCardEvent;
import com.module.playways.grab.room.fragment.GrabRoomFragment;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.functions.Consumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GrabTopRv extends RelativeLayout {
    public final static String TAG = "GrabTopRv";

    private LinkedHashMap<Integer, GrabTopItemView> mInfoMap = new LinkedHashMap<>();
    private RoomData mRoomData;
    private boolean mInited = false;
    AnimatorSet mAnimatorAllSet;

    LinearLayout mContentLl;
    ExImageView mErjiIv;

    SVGAParser mSVGAParser;
    SVGAImageView mMieDengIv;

    public GrabTopRv(Context context) {
        super(context);
        init();
    }

    public GrabTopRv(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabTopRv(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_top_content_view_layout, this);
        mContentLl = (LinearLayout) this.findViewById(R.id.content_ll);
        mErjiIv = (ExImageView) this.findViewById(R.id.erji_iv);
        mMieDengIv = this.findViewById(R.id.miedeng_iv);

//        HandlerTaskTimer.newBuilder()
//                .interval(4000)
//                .start(new HandlerTaskTimer.ObserverW() {
//                    @Override
//                    public void onNext(Integer integer) {
//                        int i=0;
//                        for(int id:mInfoMap.keySet()){
//                            if(i++==2) {
//                                lightOff(id);
//                                break;
//                            }
//                        }
//                    }
//                });

    }

    private void initData() {
        if (mInited) {
            return;
        }
        mInited = true;
        RoundInfoModel now = mRoomData.getRealRoundInfo();
        List<PlayerInfoModel> playerInfoModels = mRoomData.getPlayerInfoList();
        int i = 0;
        for (PlayerInfoModel playerInfoModel : playerInfoModels) {
            UserInfoModel userInfo = playerInfoModel.getUserInfo();
            GrabTopItemView grabTopItemView = mInfoMap.get(userInfo.getUserId());
            if (grabTopItemView == null) {
                grabTopItemView = new GrabTopItemView(getContext());
                mInfoMap.put(userInfo.getUserId(), grabTopItemView);
                RxView.clicks(grabTopItemView)
                        .subscribe(new Consumer<Object>() {
                            @Override
                            public void accept(Object o) {
                                EventBus.getDefault().post(new ShowPersonCardEvent(userInfo.getUserId()));
                            }
                        });
            }
            grabTopItemView.setVisibility(VISIBLE);
            grabTopItemView.bindData(userInfo);
            grabTopItemView.setGrap(false);
            grabTopItemView.tryAddParent(mContentLl);
//            if (i % 2 == 0) {
//                grabTopItemView.setBackgroundColor(U.getColor(R.color.yellow));
//            } else {
//                grabTopItemView.setBackgroundColor(U.getColor(R.color.blue));
//            }
            i++;
        }
        if (now != null) {
            for (int uid : now.getHasGrabUserSet()) {
                GrabTopItemView grabTopItemView = mInfoMap.get(uid);
                if (grabTopItemView != null) {
                    grabTopItemView.setGrap(true);
                }
            }
        }
    }

    public void setModeGrab() {
        // 切换到抢唱模式,
        if (mAnimatorAllSet != null) {
            mAnimatorAllSet.cancel();
        }
        mErjiIv.setVisibility(GONE);
        for (int uId : mInfoMap.keySet()) {
            GrabTopItemView grabTopItemView = mInfoMap.get(uId);
            if (grabTopItemView != null) {
                grabTopItemView.setVisibility(VISIBLE);
                grabTopItemView.reset();
                if (mRoomData.getRealRoundInfo().getHasGrabUserSet().contains(uId)) {
                    grabTopItemView.setGrap(true);
                } else {
                    grabTopItemView.setGrap(false);
                }
            }
        }
    }

    public void setModeSing(int singUid) {
        MyLog.d(TAG, "setModeSing" + " singUid=" + singUid);
        GrabTopItemView grabTopItemView = mInfoMap.get(singUid);
        if (grabTopItemView != null) {
            grabTopItemView.setGetSingChance();
        }

        List<Animator> allAnimator = new ArrayList<>();
        GrabTopItemView finalGrabTopItemView = grabTopItemView;
        {
            // 这是圈圈动画
            ObjectAnimator objectAnimator1 = new ObjectAnimator();
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
            });

            // 接下来是头像放大一点的动画
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(grabTopItemView.mAvatarIv, View.SCALE_X, 1, 1.2f);
            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(grabTopItemView.mAvatarIv, View.SCALE_Y, 1, 1.2f);
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
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(grabTopItemView, View.ALPHA, 1, 0);
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(grabTopItemView.mAvatarIv, View.SCALE_X, 1.2f, 2f);
            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(grabTopItemView.mAvatarIv, View.SCALE_Y, 1.2f, 2f);

            ObjectAnimator objectAnimator4 = new ObjectAnimator();
            objectAnimator4.setFloatValues(1, 0);
            objectAnimator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float weight = (float) animation.getAnimatedValue();
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) finalGrabTopItemView.getLayoutParams();
                    lp.weight = weight;
                    finalGrabTopItemView.setLayoutParams(lp);
                }
            });
            AnimatorSet animatorSet123 = new AnimatorSet();
            animatorSet123.playTogether(objectAnimator1, objectAnimator2, objectAnimator3, objectAnimator4);
            animatorSet123.setDuration(9 * 33);
            animatorSet123.setStartDelay(47 * 33);
            allAnimator.add(animatorSet123);
        }

        {
            // 耳机的出现
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mErjiIv, View.TRANSLATION_Y, -U.getDisplayUtils().dip2px(100), 0);
            objectAnimator1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mErjiIv.setVisibility(VISIBLE);
                }
            });
            objectAnimator1.setDuration(14 * 33);

            List<Animator> mieDengList = new ArrayList<>();
            mieDengList.add(objectAnimator1);
            int i = 0;
            // 灯的出现，以灭灯的形式出现
            for (int uId : mInfoMap.keySet()) {
                if (uId == singUid) {
                    continue;
                }
                GrabTopItemView itemView = mInfoMap.get(uId);
                ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(itemView.mFlagIv, View.SCALE_X, 1, 2);
                ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(itemView.mFlagIv, View.SCALE_Y, 1, 2);
                ObjectAnimator objectAnimator4 = ObjectAnimator.ofFloat(itemView.mFlagIv, View.ALPHA, 0, 1);
                ObjectAnimator objectAnimator5 = ObjectAnimator.ofFloat(itemView.mFlagIv, View.TRANSLATION_Y, U.getDisplayUtils().dip2px(100), 0);
                AnimatorSet animatorSet2345 = new AnimatorSet();
                animatorSet2345.playTogether(objectAnimator2, objectAnimator3, objectAnimator4, objectAnimator5);
                animatorSet2345.setDuration(7 * 33);

                ObjectAnimator objectAnimator6 = ObjectAnimator.ofFloat(itemView.mFlagIv, View.SCALE_X, 2, 1);
                ObjectAnimator objectAnimator7 = ObjectAnimator.ofFloat(itemView.mFlagIv, View.SCALE_Y, 2, 1);
                AnimatorSet animatorSet67 = new AnimatorSet();
                animatorSet67.playTogether(objectAnimator6, objectAnimator7);
                animatorSet67.setDuration(3 * 33);
                AnimatorSet animatorSet234567 = new AnimatorSet();
                animatorSet234567.playSequentially(animatorSet2345, animatorSet67);
                animatorSet234567.setStartDelay(i * 4 * 33);
                animatorSet234567.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        itemView.setLight(false);
                    }
                });
                mieDengList.add(animatorSet234567);
                i++;
            }
            AnimatorSet animatorSet1_234567s = new AnimatorSet();
            animatorSet1_234567s.playTogether(mieDengList);
            allAnimator.add(animatorSet1_234567s);
        }
        // 等 125 个节拍
        {
            List<Animator> liangdengList = new ArrayList<>();
            int i = 0;
            for (int uId : mInfoMap.keySet()) {
                if (uId == singUid) {
                    continue;
                }
                GrabTopItemView itemView = mInfoMap.get(uId);
                ObjectAnimator objectAnimator1 = new ObjectAnimator();
                objectAnimator1.setIntValues(0, 0);
                objectAnimator1.setDuration(1);
                objectAnimator1.addListener(new AnimatorListenerAdapter() {
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
                    // TODO: 2019/1/26 临时做法，等音效师改完
                    GrabTopRv.this.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            U.getSoundUtils().play(GrabRoomFragment.TAG, R.raw.lightup);
                        }
                    }, 100);
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

    private void syncLight() {
        RoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            for (int uid : now.getHasLightOffUserSet()) {
                GrabTopItemView grabTopItemView = mInfoMap.get(uid);
                if (grabTopItemView != null) {
                    grabTopItemView.setLight(false);
                }
            }
        }
    }

    public void grap(int uid) {
        GrabTopItemView grabTopItemView = mInfoMap.get(uid);
        if (grabTopItemView != null) {
            grabTopItemView.setGrap(true);
        }
    }

    public void lightOff(int uid) {
        GrabTopItemView grabTopItemView = mInfoMap.get(uid);
        if (grabTopItemView != null) {
            setLightOffAnimation(grabTopItemView);
        }
    }

    private void setLightOffAnimation(GrabTopItemView grabTopItemView) {
        grabTopItemView.mFlagIv.setVisibility(GONE);

        int[] position1 = new int[2];
        grabTopItemView.mFlagIv.getLocationInWindow(position1);

        int[] position2 = new int[2];
        mMieDengIv.getLocationInWindow(position2);

        mMieDengIv.setTranslationX(position1[0]-U.getDisplayUtils().dip2px(22));
        mMieDengIv.setTranslationY(U.getDisplayUtils().dip2px(3.5f));

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
            mSVGAParser = new SVGAParser(getContext());
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

    public void setRoomData(RoomData roomData) {
        mRoomData = roomData;
        initData();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimatorAllSet != null) {
            mAnimatorAllSet.cancel();
        }
    }
}
