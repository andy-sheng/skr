package com.wali.live.watchsdk.personalcenter.level.view;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.IdRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.proto.ExpLevelProto;
import com.wali.live.watchsdk.R;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zhujianning on 18-6-22.
 */

public class LevelPage extends FrameLayout {
    private static final String TAG = LevelPage.class.getSimpleName();

    private TextView mLevelNumber;                      // 等级数字
    private LevelProgressBarTemp mLevelProgressBar;     // 等级进度条
    private TextView mCurrentLevelTv;                   // 当前等级
    private TextView mNextLevelTv;                      // 下一等级
    private TextView mTotalExpTv;                       // 累计经验值
    private TextView mLevelUpExpTv;                     // 距离升级还差

    private float mCurrentPercent;                      // 当前绘制百分比
    private Subscription mDrawProgressSub;              // 绘制进度条的任务

    public LevelPage(@NonNull Context context) {
        this(context, null);
    }

    public LevelPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LevelPage(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_page_level, this);
        bindView();
    }

    protected void bindView() {
        mLevelNumber = $(R.id.level_number);
        mLevelProgressBar = $(R.id.level_progress_bar);
        mCurrentLevelTv = $(R.id.level_rights_progressbar_left_text);
        mNextLevelTv = $(R.id.level_rights_progressbar_right_text);
        mTotalExpTv = $(R.id.total_exp);
        mLevelUpExpTv = $(R.id.level_up_exp);

        // data binding
        bindData();
    }

    private void bindData() {
        mLevelNumber.setText(String.valueOf(MyUserInfoManager.getInstance().getLevel()));

        Observable.create(new Observable.OnSubscribe<ExpLevelProto.GetExpRsp>() {
            @Override
            public void call(Subscriber<? super ExpLevelProto.GetExpRsp> subscriber) {
                ExpLevelProto.GetExpReq getExpReq = ExpLevelProto.GetExpReq.newBuilder()
                        .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                        .build();

                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_EXPLEVEL_GET);
                data.setData(getExpReq.toByteArray());
                MyLog.d(TAG + " getLevelExp request : \n" + getExpReq.toString());
                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                if (rspData == null || rspData.getData() == null) {
                    MyLog.w(TAG, " getLevelExp rspData or data is null");
                    subscriber.onCompleted();
                    return;
                }
                try {
                    ExpLevelProto.GetExpRsp rsp = ExpLevelProto.GetExpRsp.parseFrom(rspData.getData());
                    subscriber.onNext(rsp);
                    subscriber.onCompleted();
                } catch (InvalidProtocolBufferException e) {
                    subscriber.onError(e);
                }
            }
        })
                .filter(new Func1<ExpLevelProto.GetExpRsp, Boolean>() {
                    @Override
                    public Boolean call(ExpLevelProto.GetExpRsp getExpRsp) {
                        return getExpRsp != null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(getRxActivity().<ExpLevelProto.GetExpRsp>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Action1<ExpLevelProto.GetExpRsp>() {
                    @Override
                    public void call(ExpLevelProto.GetExpRsp rsp) {
                        if (rsp.getRet() != ErrorCode.CODE_SUCCESS) {
                            MyLog.e(TAG, "get exp fail, code: " + rsp.getRet());
                            return;
                        }
                        render(rsp);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        MyLog.e(TAG, "get exp fail", e);
                    }
                });
    }

    @MainThread
    private void render(ExpLevelProto.GetExpRsp rsp) {
        if (MyUserInfoManager.getInstance().getLevel() != rsp.getLevel()) {
            MyUserInfoManager.getInstance().setLevel(rsp.getLevel());
            mLevelNumber.setText(String.valueOf(rsp.getLevel()));
            MyLog.w(TAG + " updateUserInfo and level is " +  rsp.getLevel() );
        }

        // 当前经验值
        int totalExp = rsp.getExp();
        if (totalExp <= 0) {
            totalExp = 0;
        }
        String totalStr = CommonUtils.getString(R.string.total_exp_text) + String.valueOf(totalExp);
        mTotalExpTv.setText(CommonUtils.getHighLightKeywordText(totalStr, String.valueOf(totalExp), R.color.color_red_ff2966));

        // 升级所需经验值
        int levelUpGapExp = rsp.getNextLevelExp();
        if (levelUpGapExp <= 0) {
            levelUpGapExp = 0;
        }
        String levelUpGapStr = CommonUtils.getString(R.string.level_up_exp_text) + String.valueOf(levelUpGapExp);
        mLevelUpExpTv.setText(CommonUtils.getHighLightKeywordText(levelUpGapStr, String.valueOf(levelUpGapExp), R.color.color_red_ff2966));

        String nextLevel;
        if (levelUpGapExp == 0) {//满级了
            nextLevel = "";
        } else {
            nextLevel = CommonUtils.getString(R.string.show_my_level_string_formatter, rsp.getLevel() + 1).toString();
        }
        mCurrentLevelTv.setText(CommonUtils.getString(R.string.show_my_level_string_formatter, rsp.getLevel()));
        mNextLevelTv.setText(nextLevel);

        drawProgressBar(levelUpGapExp, rsp.getLevelInterval());
    }

    /**
     * 绘制进度条动画
     * @param levelUpGapExp 升级缺少的经验
     * @param levelExpGap 当前等级升下一等级需要的经验
     */
    @MainThread
    private void drawProgressBar(int levelUpGapExp, int levelExpGap) {
        final float totalPercent;
        if (levelUpGapExp == 0) {//满级了
            totalPercent = 100F;
        } else {
            totalPercent = (float) (levelExpGap - levelUpGapExp) / levelExpGap;
        }
        MyLog.d(TAG, "total percent: " + totalPercent);
        mDrawProgressSub = Observable
                .interval(20, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(getRxActivity().<Long>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long l) {
                        mCurrentPercent += 0.01F;
                        if (mCurrentPercent >= totalPercent) {
                            mLevelProgressBar.setPercent(totalPercent);
                            mLevelProgressBar.invalidate();
                            mDrawProgressSub.unsubscribe();
                        } else {
                            mLevelProgressBar.setPercent(mCurrentPercent);
                            mLevelProgressBar.invalidate();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        MyLog.e(TAG, "draw progress fail", e);
                    }
                });
    }

    private BaseActivity getRxActivity() {
        return (BaseActivity) getContext();
    }

    private <V extends View> V $(@IdRes int id) {
        return (V) findViewById(id);
    }
}
