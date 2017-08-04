package com.wali.live.watchsdk.watch.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.push.model.BarrageMsgType;
import com.wali.live.common.barrage.event.CommentRefreshEvent;
import com.wali.live.common.gift.utils.AnimationPlayControlTemplate;
import com.wali.live.common.model.CommentModel;
import com.wali.live.watchsdk.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Star on 16/12/26.
 */
@Deprecated
public class GameBarrageViewGroup extends RelativeLayout {
    public static final String TAG = "GameBarrageViewGroup";

    private static final int FLY_SPEED = 220; // 飞行速度 220px/s

    private static final int CACHE_NUMBER = 4; // 缓存数量

//    private static final int ROAD_STATUS_IDLE = 0; // 道路状态，空闲
//    private static final int ROAD_STATUS_AVAILABLE = 1; // 道路状态，可用
//    private static final int ROAD_STATUS_BUSY = 2; // 道路状态，忙碌

    private static final int ROAD_NUM = 4;// 道路数量

    private static final int BOTTOM_SPACING = 30;// view 底部流的空隙，防止最小面的那个空间太小，显示不全

    private static final int PLAYER_SPACING = 20;// 同一条跑道中两个选手的间距

//    private int[] mRoadStatusFlag = new int[ROAD_NUM]; // 保存道路状态

    private int[] mRoadEnterNumber = new int[ROAD_NUM]; // 这条道路上有多少选手还处于进场状态，进场完成的标记是尾部进场
    private int[] mRoadRunNumber = new int[ROAD_NUM]; // 这条道路上有多少选手已经进场，处于奔跑状态

    private AnimationPlayControlTemplate<CommentModel> mFlyBarrageControl;

    private ArrayList<FlyBarrageViewWithExtraInfo> mFlyBarrageViewCache = new ArrayList(CACHE_NUMBER);

//    private ExecutorService mSingleThreadForFlyBarrage = Executors.newSingleThreadExecutor(); // 除了一些必要的ui更新操作在ui线程，其余做到都丢到这个单线程池中

    public GameBarrageViewGroup(Context context) {
        super(context);
        init();
    }

    public GameBarrageViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GameBarrageViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mFlyBarrageControl = new AnimationPlayControlTemplate<CommentModel>((RxActivity) getContext(), false, ROAD_NUM) {
            @Override
            public void onStart(CommentModel cur) {
                // 检查是否有空闲的
                if (tryFindIdleRoad(cur)) {
                    return;
                }
                // 没有空闲的，检查是否有可用的
                if (tryFindAvailableRoad(cur)) {
                    return;
                }
                // 没有结束当前
                mFlyBarrageControl.endCurrent(null);
            }

            @Override
            protected void onEnd(CommentModel model) {

            }
        };

//        FlyBarrageViewWithExtraInfo flyBarrageViewWithExtraInfo = getFlyBarrageView();
//        flyBarrageViewWithExtraInfo.view.setText("xxxxxxxxsss");
//        addViewToRoad(flyBarrageViewWithExtraInfo);
    }

    public void onActivityCreate() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void onActivityDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        EventBus.getDefault().unregister(this);
        if (mFlyBarrageControl != null) {
            mFlyBarrageControl.destroy();
        }
    }

    /**
     * 每个item占用的高度
     */
    private int mPerHeight = DisplayUtils.dip2px(30);

    private FlyBarrageViewWithExtraInfo getFlyBarrageView() {
        for (int i = 0; i < mFlyBarrageViewCache.size(); i++) {
            FlyBarrageViewWithExtraInfo info = mFlyBarrageViewCache.get(i);
            //缓存中有空闲的，返回缓存
            if (!info.isWorking) {
                return info;
            }
        }
        FlyBarrageViewWithExtraInfo info = new FlyBarrageViewWithExtraInfo();
        info.view = new TextView(getContext());
        //info.view.setTextSize(getResources().getDimensionPixelSize(R.dimen.margin_44));
        if (mFlyBarrageViewCache.size() < CACHE_NUMBER) {
            // 缓存未满，加入缓存
            mFlyBarrageViewCache.add(info);
        }
        return info;
    }

    private void removeFlyBarrageView(FlyBarrageViewWithExtraInfo info) {
        removeView(info.view);
        info.isWorking = false;
    }

    /**
     * 将view放到索引为index的道路上
     */
    private void addViewToRoad(FlyBarrageViewWithExtraInfo fbViewInfo) {
        LayoutParams lp = (LayoutParams) fbViewInfo.view.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int parentHeight = this.getHeight();
        if (parentHeight >= mPerHeight * ROAD_NUM) {
            lp.topMargin = parentHeight / ROAD_NUM * (ROAD_NUM - fbViewInfo.roadIndex - 1);
        } else {
            int temp = parentHeight - mPerHeight;
            if (temp < 0) {
                temp = 0;
            }
            lp.topMargin = temp / (ROAD_NUM - 1) * (ROAD_NUM - fbViewInfo.roadIndex - 1);
        }
        MyLog.d(TAG, "顶部为:" + lp.topMargin);
        this.addView(fbViewInfo.view, lp);
        fbViewInfo.view.setTranslationX(getWidth());
    }

    private void playFly(FlyBarrageViewWithExtraInfo fbViewInfo) {
        Observable
                .just(fbViewInfo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FlyBarrageViewWithExtraInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(FlyBarrageViewWithExtraInfo fbViewInfo) {
                        playFlyPart1(fbViewInfo);
                    }
                });
    }

    private Handler mHandler;

    private Handler getH() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    CopyOnWriteArrayList<ObjectAnimator> mAnimatorSet = new CopyOnWriteArrayList<>();

    private void playFlyPart1(final FlyBarrageViewWithExtraInfo fbViewInfo) {
        final TextView fbView = fbViewInfo.view;
        int width = fbView.getWidth();
        int part1 = width + PLAYER_SPACING;
        int time1 = (part1 * 1000) / FLY_SPEED;

        int distanceTotal = getWidth() + width;
        int timeTotal = (distanceTotal * 1000) / FLY_SPEED;
        final ObjectAnimator animator = ObjectAnimator.ofFloat(fbView, "translationX", getWidth(), -width);
        MyLog.d(TAG, "playFly ,road index=" + fbViewInfo.roadIndex
                + ",distanceTotal=" + distanceTotal
                + ",timeTotal=" + timeTotal
                + ",time1=" + time1
                + ",getWidth=" + getWidth()
                + ",-width=" + width);
        animator.setDuration(timeTotal);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fbView.setLayerType(View.LAYER_TYPE_NONE, null);
                removeFlyBarrageView(fbViewInfo);
                mRoadRunNumber[fbViewInfo.roadIndex]--;
                mAnimatorSet.remove(animator);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                fbView.setLayerType(View.LAYER_TYPE_NONE, null);
                removeFlyBarrageView(fbViewInfo);
                mRoadRunNumber[fbViewInfo.roadIndex]--;
                mAnimatorSet.remove(animator);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mAnimatorSet.add(animator);
                fbView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }

        });
        animator.start();
        getH().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < ROAD_NUM; i++) {
                    // 没人在进场并且路上也没人
                    MyLog.d(TAG, "delay 道路" + i + ",enter:" + mRoadEnterNumber[i] + ",run:" + mRoadRunNumber[i]);
                }
                mRoadEnterNumber[fbViewInfo.roadIndex]--;
                mRoadRunNumber[fbViewInfo.roadIndex]++;
                mFlyBarrageControl.endCurrent(null);
            }
        }, time1);
    }


    private boolean tryFindIdleRoad(CommentModel model) {
        if (this.getVisibility() != VISIBLE) {
            return false;
        }
        for (int i = 0; i < ROAD_NUM; i++) {
            if (mRoadEnterNumber[i] < 0) {
                mRoadEnterNumber[i] = 0;
            }
            if (mRoadRunNumber[i] < 0) {
                mRoadRunNumber[i] = 0;
            }
            // 没人在进场并且路上也没人
            MyLog.d(TAG, "道路" + i + ",enter:" + mRoadEnterNumber[i] + ",run:" + mRoadRunNumber[i]);
            if (mRoadEnterNumber[i] == 0 && mRoadRunNumber[i] == 0) {
                // 有道路空闲，进场
                MyLog.d(TAG, "道路" + i + " idle");
                FlyBarrageViewWithExtraInfo fbViewInfo = getFlyBarrageView();
                mRoadEnterNumber[i]++;
                fbViewInfo.roadIndex = i;
                fbViewInfo.isWorking = true;
                addViewToRoad(fbViewInfo);
                fbViewInfo.view.setText(model.getBody());
                if (model.getSenderId() == UserAccountManager.getInstance().getUuidAsLong()) {
                    fbViewInfo.view.setTextColor(getResources().getColor(R.color.color_5fffd0));
                } else {
                    fbViewInfo.view.setTextColor(getResources().getColor(R.color.color_white_trans_80));
                }
                playFly(fbViewInfo);
                return true;
            }
        }
        return false;
    }

    private boolean tryFindAvailableRoad(CommentModel model) {
        if (this.getVisibility() != VISIBLE) {
            return false;
        }
        for (int i = 0; i < ROAD_NUM; i++) {
            if (mRoadEnterNumber[i] == 0) {
                // 有道路空闲，播放
                MyLog.d(TAG, "道路" + i + " available");
                FlyBarrageViewWithExtraInfo fbViewInfo = getFlyBarrageView();
                mRoadEnterNumber[i]++;
                fbViewInfo.roadIndex = i;
                fbViewInfo.isWorking = true;
                addViewToRoad(fbViewInfo);
                fbViewInfo.view.setText(model.getBody());
                if (model.getSenderId() == UserAccountManager.getInstance().getUuidAsLong()) {
                    fbViewInfo.view.setTextColor(getResources().getColor(R.color.color_5fffd0));
                } else {
                    fbViewInfo.view.setTextColor(getResources().getColor(R.color.color_white_trans_80));
                }
                playFly(fbViewInfo);
                return true;
            }
        }
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CommentRefreshEvent event) {
        MyLog.d(TAG, "is visiable " + (this.getVisibility() == VISIBLE));
        if (this.getVisibility() != VISIBLE) {
            return;
        }
        CommentModel model = event.barrageMsgs.get(event.barrageMsgs.size() - 1);
        if (model == null) {
            return;
        }
        if (model.getMsgType() == BarrageMsgType.B_MSG_TYPE_TEXT) {
            mFlyBarrageControl.add(model, model.getSenderId() == UserAccountManager.getInstance().getUuidAsLong());
        }
    }

//    @Subscribe(threadMode = ThreadMode.POSTING)
//    public void onEvent(UserActionEvent.SwitchAnchor event) {
//        mFlyBarrageControl.reset();
//        for (ObjectAnimator oa : mAnimatorSet) {
//            oa.cancel();
//        }
//        for (int i = 0; i < ROAD_NUM; i++) {
//            mRoadEnterNumber[i] = 0;
//            mRoadRunNumber[i] = 0;
//        }
//        mAnimatorSet.clear();
//    }


    public String getTAG() {
        return "FlyBarrageFragment";
    }

    static class FlyBarrageViewWithExtraInfo {
        public TextView view;// view实体
        public int roadIndex;// 道路索引
        public boolean isWorking = false;// 是否正在被使用
    }
}
