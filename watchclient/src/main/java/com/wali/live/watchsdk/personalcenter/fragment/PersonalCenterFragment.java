package com.wali.live.watchsdk.personalcenter.fragment;

import android.app.Activity;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.utils.display.DisplayUtils;
import com.base.view.AlwaysMarqueeTextView;
import com.base.view.BackTitleBar;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.user.User;
import com.wali.live.common.barrage.view.utils.NobleConfigUtils;
import com.wali.live.common.view.LevelIconsLayout;
import com.wali.live.recharge.view.RechargeFragment;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.utils.level.VipLevelUtil;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.editinfo.EditInfoActivity;
import com.wali.live.watchsdk.webview.WebViewActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by zhujianning on 18-6-21.
 * 简版个人中心
 */

public class PersonalCenterFragment extends BaseFragment {
    private static final String TAG = "PersonalCenterFragment";
    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    //ui
    private SimpleDraweeView mAvatorIv;
    private AlwaysMarqueeTextView mNameTv;
    private ImageView mGenderIv;
    private TextView mIdTv;
    private LevelIconsLayout mLevelIconsContainer;
    private RelativeLayout mHeaderContainer;
    private BackTitleBar mTitleBar;

    //data
    private User mUser;
    private TextView mFollowInfoTv;
    private TextView mFanTv;
    private TextView mMsgTv;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.frag_personal_center, container, false);
    }

    @Override
    protected void bindView() {
        mTitleBar = (BackTitleBar) mRootView.findViewById(R.id.title_bar);
        mHeaderContainer = (RelativeLayout) mRootView.findViewById(R.id.header_container);
        mAvatorIv = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_iv);
        mNameTv = (AlwaysMarqueeTextView) mRootView.findViewById(R.id.name_tv);
        mGenderIv = (ImageView) mRootView.findViewById(R.id.gender_iv);
        mIdTv = (TextView) mRootView.findViewById(R.id.id_tv);
        mLevelIconsContainer = (LevelIconsLayout) mRootView.findViewById(R.id.level_icons_container);
        mFollowInfoTv = (TextView) mRootView.findViewById(R.id.follow_tv);
        mFanTv = (TextView) mRootView.findViewById(R.id.fan_tv);
        mMsgTv = (TextView) mRootView.findViewById(R.id.msg_tv);

        mTitleBar.getBackBtn().setText(GlobalData.app().getResources().getString(R.string.my));

        initListener();
        initData();
    }

    private void initListener() {
        RxView.clicks(mTitleBar.getBackBtn()).throttleFirst(300, TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                FragmentNaviUtils.popFragmentFromStack(getActivity());
            }
        });
        RxView.clicks(mHeaderContainer).throttleFirst(300, TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                EditInfoActivity.open(getActivity());
            }
        });
        RxView.clicks(mRootView.findViewById(R.id.wallet_container)).throttleFirst(300, TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                MyLog.d(TAG, "onclick wallet");
                FragmentNaviUtils.addFragment(getActivity(), R.id.main_act_container, RechargeFragment.class, null, true, true, true);
            }
        });
//        RxView.clicks(mRootView.findViewById(R.id.franchise_container)).throttleFirst(300, TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
//            @Override
//            public void call(Void aVoid) {
//                MyLog.d(TAG, "onclick franchise");
//                Bundle data = new Bundle();
//                data.putInt(ShowMyLevelActivity.EXTRA_FROM_INDEX, StatisticsKey.Level.Key.VIP_FROM_MY_TAB);
//                ShowMyLevelActivity.openActivity(getActivity(), data);
//            }
//        });
//        RxView.clicks(mRootView.findViewById(R.id.noble_franchise_container)).throttleFirst(300, TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
//            @Override
//            public void call(Void aVoid) {
//                MyLog.d(TAG, "onclick noble franchise");
//                WebViewActivity.open(getActivity(), Constants.SCHEME_JUMP_TO_MY_NOBLE);
//            }
//        });
//        RxView.clicks(mRootView.findViewById(R.id.advise_container)).throttleFirst(300, TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
//            @Override
//            public void call(Void aVoid) {
//                MyLog.d(TAG, "onclick advise");
//                Intent intent = new Intent(getActivity(), FeedBackActivity.class);
//                startActivity(intent);
//            }
//        });
    }

    private void initData() {
        mUser = MyUserInfoManager.getInstance().getUser();

        if(mUser == null) {
            MyLog.d(TAG, "user id is null");
            getActivity().finish();
        }

        updateHeader();
        updateRelationInfo();
    }

    private void updateHeader() {
        if(mUser != null) {
            //avator
            AvatarUtils.loadAvatarByUidTs(mAvatorIv, mUser.getUid(), mUser.getAvatar(), true);

            //name
            if (TextUtils.isEmpty(mUser.getNickname())) {
                mNameTv.setText(String.valueOf(mUser.getUid()));
            } else {
                MyLog.d(TAG + " updateHeader mUser.getNickname() : " + mUser.getNickname());
                mNameTv.setText(mUser.getNickname());
            }

            //uid
            mIdTv.setText(GlobalData.app().getResources().getString(R.string.default_id_hint) + String.valueOf(mUser.getUid()));

            //显示性别
            if (mUser.getGender() == 1) {
                mGenderIv.setVisibility(View.VISIBLE);
                mGenderIv.setBackgroundResource(R.drawable.all_man);
            } else if (mUser.getGender() == 2) {
                mGenderIv.setVisibility(View.VISIBLE);
                mGenderIv.setBackgroundResource(R.drawable.all_women);
            } else {
                mGenderIv.setVisibility(View.GONE);
            }

            updateLevelIcons();
        }
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
            view = LevelIconsLayout.getDefaultTextView(getActivity());
            view.setBackgroundResource(pair.second);
            //view.setText(String.valueOf(mUser.getVipLevel()));
            list.add(view);
        }
        // Plain
        GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(mUser.getLevel());
        view = LevelIconsLayout.getDefaultTextView(getActivity());
        view.setText(String.valueOf(mUser.getLevel()) + " ");
        view.setBackgroundDrawable(levelItem.drawableBG);
        view.setCompoundDrawables(levelItem.drawableLevel, null, null, null);
        if (mUser.getVipLevel() > 4 && !mUser.isVipFrozen()) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(view.getLayoutParams());
            params.setMargins(DisplayUtils.dip2px(3), DisplayUtils.dip2px(2), 0, 0);
            view.setLayoutParams(params);
        }
        list.add(view);

//        if (mCharmLevel > 0) {//TODO-暂时去了(如果主播开了粉丝团显示等级)
//            view = LevelIconsLayout.getDefaultTextView(PersonalCenterActivity.this);
//            view.setBackgroundResource(VfansInfoUtils.getImageResoucesByCharmLevelValue(mCharmLevel));
//            if (mUser.getVipLevel() > 4 && !mUser.isVipFrozen()) {
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(view.getLayoutParams());
//                params.setMargins(DisplayUtils.dip2px(3), DisplayUtils.dip2px(2), 0, 0);
//                view.setLayoutParams(params);
//            }
//            list.add(view);
//        }

        MyLog.d(TAG, "zjnTest list size():" + list.size());
        mLevelIconsContainer.addIconsWithClear(list);
    }

    private void updateRelationInfo() {
        if(mUser != null) {
            mFollowInfoTv.setText(String.format(GlobalData.app().getResources().getString(R.string.follow_cnt), String.valueOf(mUser.getFollowNum())));
            mFanTv.setText(String.format(GlobalData.app().getResources().getString(R.string.fan_cnt), String.valueOf(mUser.getFansNum())));
            mMsgTv.setText(GlobalData.app().getResources().getString(R.string.msg));
        }
    }

    public static void openFragment(Activity activity, int containerId) {
        FragmentNaviUtils.addFragment((BaseSdkActivity) activity, containerId, PersonalCenterFragment.class, null, true, true, true);
    }
}
