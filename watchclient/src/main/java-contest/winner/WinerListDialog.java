package winner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wali.live.watchsdk.R;
import model.AwardUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by jiyangli on 18-1-15.
 */
public class WinerListDialog extends Dialog {
    private static final String TAG = "WinerListDialog";

    private SimpleDraweeView imgAnim;
    private RelativeLayout llytRoad;
    private List<AwardUser> data = new ArrayList<>();
    private float award;
    private TextView mWinNumTv;

    private static final int FLY_SPEED = 180; // 飞行速度 180px/s

    int index = 0;

    public WinerListDialog(@NonNull Context context) {
        this(context, R.style.PKDialog);
    }

    public WinerListDialog(@NonNull Context context, int themeResId) {
        super(context, R.style.PKDialog);
        init();
    }

    public void show(List<AwardUser> data, float award, int winNum) {
        this.index = 0;
        this.data.clear();
        this.data.addAll(data);
        this.award = award;

        if (this.data.size() > 0) {
            show();
            mWinNumTv.setText(getContext().getResources().getString(R.string.contest_success_num, winNum));
            FrescoWorker.frescoShowWebp(imgAnim, R.raw.animated_winers, DisplayUtils.getScreenWidth() / 16, DisplayUtils.getPhoneHeight() / 16);
            tryFindIdleRoad();
            this.index++;
            long showTime;
            if (data.size() < 10) {
                showTime = 10 * 1000;
            } else if (data.size() > 20) {
                showTime = 20 * 1000;
            } else {
                showTime = data.size() * 1000;
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hide();
                }
            }, showTime);
        }
    }

    private void init() {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.winer_list_layout, null);
        imgAnim = (SimpleDraweeView) contentView.findViewById(R.id.winer_list_imgAnim);
        llytRoad = (RelativeLayout) contentView.findViewById(R.id.winer_list_llytRoad);

        mWinNumTv = (TextView) contentView.findViewById(R.id.win_num_tv);

        setContentView(contentView);
        setCanceledOnTouchOutside(true);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onActivityDestroy();
            }
        });
    }

    public void onActivityDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (null != mAnimatorSet && mAnimatorSet.size() > 0) {
            for (ObjectAnimator oa : mAnimatorSet) {
                oa.cancel();
            }
        }
    }

    /**
     * 每个item占用的高度
     */
    private int mPerHeight = DisplayUtils.dip2px(116.67f);

    private WinerListDialog.FlyBarrageViewWithExtraInfo getFlyBarrageView() {
        if (index < data.size() && data.get(index) != null) {

            WinerListDialog.FlyBarrageViewWithExtraInfo info = new WinerListDialog.FlyBarrageViewWithExtraInfo();
            info.view = new WinerItemView(getContext());
            info.view.setData(data.get(index), award);
            return info;
        } else {
            return null;
        }
    }

    /**
     * 将view放到索引为index的道路上
     */
    private void addViewToRoad(WinerListDialog.FlyBarrageViewWithExtraInfo fbViewInfo) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) fbViewInfo.view.getLayoutParams();
        if (lp == null) {
            lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
//        int parentHeight = DisplayUtils.dip2px(200);
//
//        int temp = parentHeight - mPerHeight;
//        if (temp < 0) {
//            temp = 0;
//        }
        if (fbViewInfo.roadIndex != 0) {
            lp.topMargin = 223;
        } else {
            lp.topMargin = 0;
        }
        MyLog.d(TAG, "顶部为:" + lp.topMargin);

        llytRoad.addView(fbViewInfo.view, lp);
        fbViewInfo.view.setTranslationX(GlobalData.screenWidth);
    }

    private Handler mHandler;

    private Handler getH() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    CopyOnWriteArrayList<ObjectAnimator> mAnimatorSet = new CopyOnWriteArrayList<>();

    private void playFlyPart1(final WinerListDialog.FlyBarrageViewWithExtraInfo fbViewInfo) {
        final WinerItemView fbView = fbViewInfo.view;
        int width = 200;

        int distanceTotal;
        distanceTotal = GlobalData.screenWidth + width;
        int timeTotal = (distanceTotal * 1000) / FLY_SPEED;

        final ObjectAnimator animator = ObjectAnimator.ofFloat(fbView, "translationX", GlobalData.screenWidth, -width);
        MyLog.d(TAG, "playFly ,road index=" + fbViewInfo.roadIndex + ",distanceTotal=" + distanceTotal + ",timeTotal=" + timeTotal);
        animator.setDuration(timeTotal);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fbView.setLayerType(View.LAYER_TYPE_NONE, null);
                llytRoad.removeView(fbViewInfo.view);
                mAnimatorSet.remove(animator);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                fbView.setLayerType(View.LAYER_TYPE_NONE, null);
                llytRoad.removeView(fbViewInfo.view);
                mAnimatorSet.remove(animator);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                fbView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
        });
        animator.start();
        mAnimatorSet.add(animator);

        getH().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (WinerListDialog.this.index < data.size()) {
                    WinerListDialog.this.index++;
                    tryFindIdleRoad();
                }
            }
        }, 1100);
    }


    private boolean tryFindIdleRoad() {
        int i = (index + 1) % 2;
        MyLog.d(TAG, "道路" + i + " idle");
        WinerListDialog.FlyBarrageViewWithExtraInfo fbViewInfo = getFlyBarrageView();

        if (fbViewInfo == null) {
            MyLog.d(TAG, "FlyBarrageViewWithExtraInfo is null");
            return false;
        }
        fbViewInfo.roadIndex = i;

        addViewToRoad(fbViewInfo);

        playFlyPart1(fbViewInfo);
        return true;
    }

    public String getTAG() {
        return "FlyBarrageFragment";
    }

    static class FlyBarrageViewWithExtraInfo {
        public WinerItemView view;// view实体
        public int roadIndex;// 道路索引
    }
}
