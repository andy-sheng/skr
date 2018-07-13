package com.wali.live.watchsdk.personalcenter.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.util.Pair;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.dialog.DialogUtils;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.channel.ReleaseChannelUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.base.utils.version.VersionManager;
import com.base.view.AlwaysMarqueeTextView;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.user.User;
import com.wali.live.common.barrage.view.utils.NobleConfigUtils;
import com.wali.live.common.view.LevelIconsLayout;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.utils.level.VipLevelUtil;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

public class MyInfoSummaryView extends RelativeLayout {
    public final static String TAG = "MyInfoSummaryView";

    private Context mContext;

    //ui
    private View mRealView;
    private BaseImageView mAvatarIv;
    private AlwaysMarqueeTextView mNameTv;
    private ImageView mGenderIv;
    private TextView mIdTv;
    private LevelIconsLayout mLevelIconsContainer;
    private TextView mSignTv;
    private TextView mFollowTv;
    private TextView mFanTv;
    private TextView mModifyTv;

    //data
    private User mUser;

    public MyInfoSummaryView(Context context) {
        super(context);
        init(context);
    }

    private void init(final Context context) {
        mContext = context;
        mRealView = inflate(context, R.layout.my_info_personal_summary_layout, this);
        mAvatarIv = (BaseImageView) mRealView.findViewById(R.id.avatar_iv);
        mNameTv = (AlwaysMarqueeTextView) mRealView.findViewById(R.id.name_tv);
        mGenderIv = (ImageView) mRealView.findViewById(R.id.gender_iv);
        mIdTv = (TextView) mRealView.findViewById(R.id.id_tv);
        mLevelIconsContainer = (LevelIconsLayout) mRealView.findViewById(R.id.level_icons_container);
        mSignTv = (TextView) mRealView.findViewById(R.id.sign_tv);
        mFollowTv = (TextView) mRealView.findViewById(R.id.follow_tv);
        mFanTv = (TextView) mRealView.findViewById(R.id.fan_tv);
        mModifyTv = (TextView) mRealView.findViewById(R.id.modify_tv);

        mUser = MyUserInfoManager.getInstance().getUser();

        initListener();
        bindData();

        mAvatarIv.setOnClickListener(new OnClickListener() {
            int count = 0;
            long ts = 0;
            boolean debug = false;

            @Override
            public void onClick(View v) {
                if (debug) {
                    if (CommonUtils.isFastDoubleClick()) {
                        try {
                            Class debugActivity = Class.forName("com.wali.live.MainActivity");
                            Intent intent = new Intent(context, debugActivity);
                            getContext().startActivity(intent);
                        } catch (ClassNotFoundException e) {
                        }
                    }
                } else {
                    if (System.currentTimeMillis() - ts < 500) {
                        count++;
                    } else {
                        count = 0;
                    }
                    ts = System.currentTimeMillis();
                    if (count == 20) {
                        debug = true;
                        count = 0;
                    }
                }
            }
        });
    }

    private void initListener() {
        RxView.clicks(mModifyTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EventBus.getDefault().post(new EventClass.JumpHalfEditFragEvent());
                    }
                });
        RxView.clicks(mFanTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EventBus.getDefault().post(new EventClass.JumpHalfFansFragEvent());
                    }
                });
        RxView.clicks(mFollowTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EventBus.getDefault().post(new EventClass.JumpHalfFollowsFragEvent());
                    }
                });
    }

    private void bindData() {
        if (mUser == null) {
            MyLog.w(TAG, "mUser is null");
            return;
        }

        AvatarUtils.loadAvatarByUidTs(mAvatarIv, mUser.getUid(), mUser.getAvatar(), true);
        mNameTv.setText(TextUtils.isEmpty(mUser.getNickname()) ? String.valueOf(mUser.getUid()) : mUser.getNickname());

        if (mUser.getGender() == 1) {
            mGenderIv.setVisibility(View.VISIBLE);
            mGenderIv.setBackgroundResource(R.drawable.all_man);
        } else if (mUser.getGender() == 2) {
            mGenderIv.setVisibility(View.VISIBLE);
            mGenderIv.setBackgroundResource(R.drawable.all_women);
        } else {
            mGenderIv.setVisibility(View.GONE);
        }

        mIdTv.setText(GlobalData.app().getResources().getString(R.string.default_id_hint) + String.valueOf(mUser.getUid()));
        updateLevelIcons();
        mSignTv.setText(TextUtils.isEmpty(mUser.getSign()) ? CommonUtils.getString(R.string.default_sign_txt) : mUser.getSign());

        mFollowTv.setText(String.format(GlobalData.app().getResources().getString(R.string.follow_cnt), String.valueOf(mUser.getFollowNum())));
        mFanTv.setText(String.format(GlobalData.app().getResources().getString(R.string.fan_cnt), String.valueOf(mUser.getFansNum())));
    }

    public void refreshUi() {
        MyLog.d(TAG, "refreshUi");
        bindData();
    }

    private void updateLevelIcons() {
        List<TextView> list = new ArrayList<>();
        TextView view;
        if (mUser.isNoble()) {
            view = LevelIconsLayout.getDefaultTextView(GlobalData.app());
            view.setBackgroundResource(NobleConfigUtils.getImageResoucesByNobelLevelInBarrage(mUser.getNobleLevel()));
            list.add(view);
        }
        // VIP
        Pair<Boolean, Integer> pair = VipLevelUtil.getLevelBadgeResId(mUser.getVipLevel(), mUser.isVipFrozen(), false);
        if (true == pair.first) {
            view = LevelIconsLayout.getDefaultTextView(mContext);
            view.setBackgroundResource(pair.second);
            list.add(view);
        }
        // Plain
        GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(mUser.getLevel());
        view = LevelIconsLayout.getDefaultTextView(mContext);
        view.setText(String.valueOf(mUser.getLevel()) + " ");
        view.setBackgroundDrawable(levelItem.drawableBG);
        view.setCompoundDrawables(levelItem.drawableLevel, null, null, null);
        if (mUser.getVipLevel() > 4 && !mUser.isVipFrozen()) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(view.getLayoutParams());
            params.setMargins(DisplayUtils.dip2px(3), DisplayUtils.dip2px(2), 0, 0);
            view.setLayoutParams(params);
        }
        list.add(view);

//        if (mCharmLevel > 0) {//TODO-暂时去了
//            (如果主播开了粉丝团显示等级)
//            view = LevelIconsLayout.getDefaultTextView(PersonalCenterActivity.this);
//            view.setBackgroundResource(VfansInfoUtils.getImageResoucesByCharmLevelValue(mCharmLevel));
//            if (mUser.getVipLevel() > 4 && !mUser.isVipFrozen()) {
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(view.getLayoutParams());
//                params.setMargins(DisplayUtils.dip2px(3), DisplayUtils.dip2px(2), 0, 0);
//                view.setLayoutParams(params);
//            }
//            list.add(view);
//        }

        mLevelIconsContainer.addIconsWithClear(list);
    }
}
