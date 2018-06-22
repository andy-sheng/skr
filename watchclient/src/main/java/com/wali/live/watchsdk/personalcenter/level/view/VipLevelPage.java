package com.wali.live.watchsdk.personalcenter.level.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.image.fresco.processor.GrayPostprocessor;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.language.LocaleUtil;
import com.base.utils.span.SpanUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.protobuf.InvalidProtocolBufferException;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.income.model.ExceptionWithCode;
import com.wali.live.proto.UserProto;
import com.wali.live.proto.Vip.VipProto;
import com.wali.live.utils.level.VipLevelUtil;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.personalcenter.level.LevelActivity;
import com.wali.live.watchsdk.personalcenter.level.model.UserVipInfo;
import com.wali.live.watchsdk.personalcenter.level.net.VipHomePageRequest;
import com.wali.live.watchsdk.webview.WebViewActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.base.utils.CommonUtils.getString;

/**
 * VIP等级View
 * Created by rongzhisheng on 17-4-27.
 */
public class VipLevelPage extends FrameLayout implements VipPrivilegeTableLayout.OnVipPrivilegeItemClickListener {
    private static final String TAG = VipLevelPage.class.getSimpleName();

    private static final int LOCAL_MAX_VIP_LEVEL = VipLevelUtil.MAX_LEVEL_IMAGE_NO;   // 本地有的最高等级图标，超过该等级需要去网络下载

    private SimpleDraweeView mLevelBadge;               // 等级徽章
    private TextView mLevelName;                        // 等级名称
//    private View mHideArea;                             // 隐身开关区域
//    private SwitchButton mHideBtn;                      // 隐身开关
//    private TextView mHideTip;                          // 隐身提示
    private LevelProgressBarTemp mLevelProgressBar;     // 等级进度条
    private TextView mCurrentLevelTv;                   // 当前等级
    private TextView mNextLevelTv;                      // 下一等级
    private VipPrivilegeTableLayout mPrivilegeListView; // VIP特权列表
    private TextView mTotalSentGoldGemTv;               // 送出金钻总数
    private TextView mExpInfoTv;                        // 经验信息
    private TextView mVipInstructionBtn;                // VIP介绍按钮

    private float mCurrentPercent;                      // 当前绘制百分比
    private Subscription mDrawProgressSub;              // 绘制进度条的任务

    public VipLevelPage(@NonNull Context context) {
        this(context, null);
    }

    public VipLevelPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VipLevelPage(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_vip_level, this);
        bindView();
    }

    @MainThread
    private void bindView() {
        mLevelBadge = $(R.id.level_icon);
        mLevelName = $(R.id.level_name);
//        mHideArea = $(R.id.hide_area);
//        mHideBtn = $(R.id.hide_switch);
//        mHideTip = $(R.id.hide_tip);
        mLevelProgressBar = $(R.id.level_progress_bar);
        mCurrentLevelTv = $(R.id.level_current_level);
        mNextLevelTv = $(R.id.level_next_level);
        mPrivilegeListView = $(R.id.privilege_list);
        mTotalSentGoldGemTv = $(R.id.total_sent_gold_gem);
        mExpInfoTv = $(R.id.exp_info);
        mVipInstructionBtn = $(R.id.vip_instruction_btn);

//        mHideBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                // 设置隐身状态，设置失败时需要回滚UI
//                setVipHideStatus(isChecked)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Subscriber<UserProto.UploadUserSettingRsp>() {
//                            @Override
//                            public void onCompleted() {
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                MyLog.e(TAG, "update vip hide status to " + isChecked + " fail", e);
//                                mHideBtn.setCheckedImmediatelyNoEvent(!isChecked);
//                            }
//
//                            @Override
//                            public void onNext(UserProto.UploadUserSettingRsp rsp) {
//                                MyUserInfoManager.getInstance().setVipInfo(isChecked);
//                            }
//                        });
//            }
//        });

        bindData();
    }

    private BaseActivity getRxActivity() {
        return (BaseActivity) getContext();
    }

    @MainThread
    private void bindData() {
        mLevelName.setText("");
        boolean isRibbonDisable = MyUserInfoManager.getInstance().isVipFrozen()
                || MyUserInfoManager.getInstance().getVipLevel() <= 0;
        mLevelName.setBackgroundResource(isRibbonDisable ? R.drawable.vip_icon_bg_unable : R.drawable.vip_icon_bg_normal);
        FrescoWorker.loadImage(mLevelBadge, getVipBadgeImage(MyUserInfoManager.getInstance().getVipLevel(),
                MyUserInfoManager.getInstance().isVipFrozen()));
        mVipInstructionBtn.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        RxView.clicks(mVipInstructionBtn).throttleFirst(2, TimeUnit.SECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                onVipPrivilegeItemClick(0);
            }
        });

        Observable.create(new Observable.OnSubscribe<VipProto.VipHomePageRsp>() {
            @Override
            public void call(Subscriber<? super VipProto.VipHomePageRsp> subscriber) {
                VipProto.VipHomePageRsp rsp = new VipHomePageRequest().syncRsp();
                subscriber.onNext(rsp);
                subscriber.onCompleted();
            }
        })
                .flatMap(new Func1<VipProto.VipHomePageRsp, Observable<VipProto.VipHomePageRsp>>() {
                    @Override
                    public Observable<VipProto.VipHomePageRsp> call(VipProto.VipHomePageRsp rsp) {
                        if (rsp == null) {
                            return Observable.error(new Throwable("VipHomePageRsp is null"));
                        }
                        if (rsp.getRet() != ErrorCode.CODE_SUCCESS) {
                            return Observable.error(new ExceptionWithCode("get VipHomePageRsp fail", rsp.getRet()));
                        }
                        return Observable.just(rsp);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(getRxActivity().<VipProto.VipHomePageRsp>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Subscriber<VipProto.VipHomePageRsp>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "get vip info fail", e);
                    }

                    @Override
                    public void onNext(VipProto.VipHomePageRsp rsp) {
                        render(rsp);
                    }
                });
    }

    @MainThread
    private void render(VipProto.VipHomePageRsp rsp) {
        UserVipInfo userVipInfo = UserVipInfo.newInstance(rsp);
        MyLog.w(TAG, "userVipInfo:" + userVipInfo);
        if (MyUserInfoManager.getInstance().getVipLevel() != userVipInfo.getLevel()
                || MyUserInfoManager.getInstance().isVipFrozen() != userVipInfo.isFrozen()
                ) {
            MyUserInfoManager.getInstance().setVipInfo(userVipInfo.getLevel(),
                    userVipInfo.isFrozen());
            FrescoWorker.loadImage(mLevelBadge, getVipBadgeImage(userVipInfo.getLevel(),
                    userVipInfo.isFrozen()));
        }
        EventBus.getDefault().post(new LevelActivity.FetchedVipFrozenInfoEvent());

        mLevelName.setText(userVipInfo.getLevelName());
        boolean isRibbonDisable = userVipInfo.isFrozen()
                || userVipInfo.getLevel() <= 0;
        mLevelName.setBackgroundResource(isRibbonDisable ? R.drawable.vip_icon_bg_unable : R.drawable.vip_icon_bg_normal);

        mTotalSentGoldGemTv.setText(getTotalSentGoldGemText(userVipInfo.getExp()));
        if (userVipInfo.isMaxLevel()) {
            mExpInfoTv.setVisibility(GONE);
        } else {
            mExpInfoTv.setVisibility(VISIBLE);
            mExpInfoTv.setText(getUpdateTipText(userVipInfo.getLevel() + 1, userVipInfo.getUpdateRequiredExp()));
        }

        //showHideSwitch(userVipInfo.canHide(), userVipInfo.isFrozen(), userVipInfo.isHide());
        setHideStatus(userVipInfo.canHide(), userVipInfo.isFrozen());
        drawProgressBar(userVipInfo.getUpdateRequiredExp(), userVipInfo.getNextLevelExpGap());
        List<UserVipInfo.VipPrivilege> vipPrivilegeList = userVipInfo.getVipPrivilegeList();
        vipPrivilegeList.add(new UserVipInfo.VipPrivilege(getString(R.string.vip_prililege_more_text).toString(),
                UserVipInfo.VipPrivilege.TYPE_MORE, 0, true));
        mPrivilegeListView.setOnVipPrivilegeItemClickListener(this);
        mPrivilegeListView.setVipPrivilegeList(vipPrivilegeList, R.layout.vip_privilege_list_item);

        String nextLevel;
        if (userVipInfo.getUpdateRequiredExp() == 0) {//满级了
            nextLevel = "";
        } else {
            nextLevel = CommonUtils.getString(R.string.show_my_vip_level_string_formatter, userVipInfo.getLevel() + 1).toString();
        }
        mCurrentLevelTv.setText(getString(R.string.show_my_vip_level_string_formatter, userVipInfo.getLevel()));
        mNextLevelTv.setText(nextLevel);
    }

    @NonNull
    private CharSequence getUpdateTipText(int nextLevel, int updateRequiredGem) {
        String vipLevel = CommonUtils.getString(R.string.vip_level, nextLevel).toString();
        String tip = CommonUtils.getString(R.string.vip_level_update_required_exp_tip,
                vipLevel, updateRequiredGem).toString();
        CharSequence highlightedKeywordText = getHighLightKeywordText(tip, String.valueOf(updateRequiredGem),
                vipLevel, R.color.color_red_ff2966);
        return highlightedKeywordText;
    }

    @NonNull
    private CharSequence getTotalSentGoldGemText(int totalSentGoldGem) {
        String string = getString(R.string.vip_total_get_exp, totalSentGoldGem).toString();
        CharSequence highlightedKeywordText = CommonUtils.getHighLightKeywordText(string,
                String.valueOf(totalSentGoldGem), R.color.color_red_ff2966);
        return highlightedKeywordText;
    }
//
//    @NonNull
//    private CharSequence appendIcon(CharSequence highlightedKeywordText, @DrawableRes int iconId) {
//        SpannableStringBuilder ssb = new SpannableStringBuilder(highlightedKeywordText);
//        ssb.append(" ");// 加个空格与图片保持距离
//        int length = ssb.length();
//        Drawable goldGem = getResources().getDrawable(iconId);
//        goldGem.setBounds(0, 0, goldGem.getIntrinsicWidth(), goldGem.getIntrinsicHeight());
//        ssb.append("a");
//        SpanUtils.CenterImageSpan imageSpan = new SpanUtils.CenterImageSpan(goldGem);
//        ssb.setSpan(imageSpan, length, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        return ssb;
//    }

    private <V extends View> V $(@IdRes int id) {
        return (V) findViewById(id);
    }

    private BaseImage getVipBadgeImage(int level, boolean isFrozen) {
        if (level <= 0) {
            // 设置宽高后就没了锯齿
            return ImageFactory.newResImage(R.drawable.vip_icon_lv_0).setWidth(285).setHeight(330).build();
        }
        BaseImage baseImage;
        if (level <= LOCAL_MAX_VIP_LEVEL) {
            @DrawableRes int vipBadgeId;
            if (!isFrozen) {
                String iconName = "vip_icon_lv_" + level;
                try {
                    vipBadgeId = (int) R.drawable.class.getField(iconName).get(null);
                } catch (Exception e) {
                    MyLog.e(TAG, "not found picture:" + iconName, e);
                    vipBadgeId = R.drawable.vip_icon_lv_7;
                }
            } else {
                // 1- 4级VIP等级徽章没尖角，5-7级有尖角
                vipBadgeId = level < 5 ? R.drawable.vip_icon_lv_disable : R.drawable.vip_icon_lv1_disable;
            }
            baseImage = ImageFactory.newResImage(vipBadgeId).setWidth(285).setHeight(330).build();
        } else {
            baseImage = ImageFactory.newHttpImage(GetConfigManager.getInstance().getVipLevelIconUrlPrefix() + "lv_" + level).build();
            baseImage.setFailureDrawable(CommonUtils.getDrawable(R.drawable.vip_icon_lv_0));
            if (isFrozen) {
                baseImage.setPostprocessor(new GrayPostprocessor());
            }
        }
        return baseImage;
    }

//    @MainThread
//    private void showHideSwitch(boolean canHide, boolean isFrozen, boolean isHide) {
//        // 如果用户曾经解锁了隐身特权
//        if (canHide) {
//            mHideBtn.setCheckedImmediatelyNoEvent(isHide);
//            mHideBtn.setEnabled(!isFrozen);
//            mHideArea.setVisibility(VISIBLE);
//        } else {
//            mHideArea.setVisibility(GONE);
//        }
//    }

    /**
     * 绘制进度条动画
     *
     * @param levelUpGapExp 升级缺少的经验
     * @param levelExpGap   当前等级升下一等级需要的经验
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

//    @NonNull
//    private Observable<UserProto.UploadUserSettingRsp> setVipHideStatus(final boolean isHide) {
//        return Observable.create(new Observable.OnSubscribe<UserProto.UploadUserSettingRsp>() {
//            @Override
//            public void call(Subscriber<? super UserProto.UploadUserSettingRsp> subscriber) {
//                UserProto.UploadUserSettingReq req = UserProto.UploadUserSettingReq.newBuilder()
//                        .setZuid(UserAccountManager.getInstance().getUuidAsLong()).setIsVipHide(isHide)
//                        .build();
//                UserProto.UploadUserSettingRsp rsp = UserInfoManager.UploadUserSettingRspToServer(req);
//                subscriber.onNext(rsp);
//                subscriber.onCompleted();
//            }
//        })
//                .flatMap(new Func1<UserProto.UploadUserSettingRsp, Observable<UserProto.UploadUserSettingRsp>>() {
//                    @Override
//                    public Observable<UserProto.UploadUserSettingRsp> call(UserProto.UploadUserSettingRsp rsp) {
//                        if (rsp == null) {
//                            return Observable.error(new Throwable("UploadUserSettingRsp is null"));
//                        }
//                        if (rsp.getErrorCode() != ErrorCode.CODE_SUCCESS) {
//                            return Observable.error(new ExceptionWithCode(rsp.getErrorCode()));
//                        }
//                        return Observable.just(rsp);
//                    }
//                });
//    }

    public static CharSequence getHighLightKeywordText(@NonNull String text, @NonNull String keyword,
                                                       @NonNull String distraction, @ColorRes int colorResId) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        if (TextUtils.isEmpty(keyword)) {
            return text;
        }

        int start = text.indexOf(keyword);
        if (start < 0) {
            return text;
        }

        if (distraction.contains(keyword)) {
            int s = text.indexOf(distraction);
            // 如果干扰项不存在，说明找的start是keyword的start
            if (s >= 0) {
                // 确定干扰项和keyword的前后关系
                if (s <= start) {// 干扰项在keyword前面，但keyword可能并不存在
                    s = text.indexOf(keyword, s + distraction.length());
                    if (s < 0) {// keyword不存在
                        return text;
                    }
                    start = s;
                }
            }
        }

        int end = start + keyword.length();
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        ssb.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(colorResId)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    @Override
    public void onVipPrivilegeItemClick(int type) {
        String url = CommonUtils.getWebViewUrl(LocaleUtil.VIP_INSTRUCTION_URL);
        if (type > 0) {
            url += String.format("#%02d", type);
        }
        WebViewActivity.open((Activity) this.getContext(), url);
    }

    private void setHideStatus(boolean canHide, boolean isFrozen) {
        //                        showHideSwitch(canHide, isFrozen, rsp.getIsVipHide());
       Observable.create(new Observable.OnSubscribe<UserProto.GetOwnSettingRsp>() {
            @Override
            public void call(Subscriber<? super UserProto.GetOwnSettingRsp> subscriber) {
                UserProto.GetOwnSettingReq req = UserProto.GetOwnSettingReq.newBuilder()
                        .setZuid(UserAccountManager.getInstance().getUuidAsLong()).build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_GET_OWN_SETTING);
                data.setData(req.toByteArray());
                MyLog.d(TAG, "GetOwnSettingReq:\n" + req.toString());
                PacketData packetData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                if (packetData == null) {
                    subscriber.onError(new Throwable("packet data is null"));
                } else {
                    try {
                        subscriber.onNext(UserProto.GetOwnSettingRsp.parseFrom(packetData.getData()));
                    } catch (InvalidProtocolBufferException e) {
                        subscriber.onError(new Throwable("parse GetOwnSettingRsp fail"));
                    }
                    subscriber.onCompleted();
                }
            }
        })
                .flatMap(new Func1<UserProto.GetOwnSettingRsp, Observable<UserProto.GetOwnSettingRsp>>() {
                    @Override
                    public Observable<UserProto.GetOwnSettingRsp> call(UserProto.GetOwnSettingRsp rsp) {
                        if (rsp == null) {
                            return Observable.error(new Throwable("GetOwnSettingRsp is null"));
                        }
                        if (rsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                            return Observable.error(new ExceptionWithCode(rsp.getRetCode()));
                        }
                        return Observable.just(rsp);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(getRxActivity().<UserProto.GetOwnSettingRsp>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Subscriber<UserProto.GetOwnSettingRsp>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, e);
                    }

                    @Override
                    public void onNext(UserProto.GetOwnSettingRsp rsp) {
                        MyLog.w(TAG, "GetOwnSettingRsp:" + rsp);
//                        showHideSwitch(canHide, isFrozen, rsp.getIsVipHide());
                        if (MyUserInfoManager.getInstance().isVipHide() != rsp.getIsVipHide()) {
                            MyUserInfoManager.getInstance().setVipInfo(rsp.getIsVipHide());
                        }
                    }
                });

    }
}
