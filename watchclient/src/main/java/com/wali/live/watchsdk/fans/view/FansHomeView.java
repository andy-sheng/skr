package com.wali.live.watchsdk.fans.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.global.GlobalData;
import com.base.mvp.specific.RxRelativeLayout;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.common.barrage.view.utils.FansInfoUtils;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.FansPrivilegeFragment;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;
import com.wali.live.watchsdk.fans.presenter.FansHomePresenter;
import com.wali.live.watchsdk.fans.view.merge.FansDetailBasicView;
import com.wali.live.watchsdk.view.EmptyView;

import java.util.List;

import static com.wali.live.watchsdk.fans.FansPrivilegeFragment.TYPE_BAN_BARRAGE;
import static com.wali.live.watchsdk.fans.FansPrivilegeFragment.TYPE_CHARM_MEDAL;
import static com.wali.live.watchsdk.fans.FansPrivilegeFragment.TYPE_COLOR_BARRAGE;
import static com.wali.live.watchsdk.fans.FansPrivilegeFragment.TYPE_FREE_FLY_BARRAGE;
import static com.wali.live.watchsdk.fans.FansPrivilegeFragment.TYPE_MORE_FANS;
import static com.wali.live.watchsdk.fans.FansPrivilegeFragment.TYPE_TOUR_DIVIDE;
import static com.wali.live.watchsdk.fans.FansPrivilegeFragment.TYPE_UPGRADE_ACCELERATION;

/**
 * Created by zyh on 2017/11/8.
 *
 * @module 粉丝团的首页
 */

public class FansHomeView extends RxRelativeLayout implements View.OnClickListener, FansHomePresenter.IView {
    private final String TAG = "FansHomeView";

    public static final int UPGRADE_ACCELERATE_LEVEL = 1;
    public static final int SEND_COLOR_BARRAGE_VIP_LEVEL = 3;
    public static final int SEND_FLY_BARRAGE_LEVEL = 5;
    public static final int BAN_LEVEL = 8;

    private FansGroupDetailModel mGroupDetailModel;

    private EmptyView mEmptyView;
    private ScrollView mMainArea;

    private FansDetailBasicView mDetailBasicView;

    private ViewGroup mMyInfoContainer;
    private ImageView mJoinBannerIv;
    private RelativeLayout mMyInfoArea;

    private TextView mMyMedalTv;
    private TextView mFanValueTv;
    private TextView mFanRankTv;

    private TextView mPrivilegeTitleTv;
    //会员加速特权
    private ImageView mAcceleratePrivilegeIv;
    private TextView mAcceleratePrivilegeTitleTv;
    private TextView mAcceleratePrivilegeStatusTv;
    //彩色弹幕特权
    private ImageView mColorBarrageIv;
    private TextView mColorBarrageTitleTv;
    private TextView mColorBarrageStatusTv;
    //飞屏特权
    private ImageView mFlyBarrageIv;
    private TextView mFlyBarrageTitleTv;
    private TextView mFlyBarrageStatusTv;
    //禁言特权
    private ImageView mForbiddenIv;
    private TextView mForbiddenTitleTv;
    private TextView mForbiddenStatusTv;

    private FansHomePresenter mPresenter;

    private String mAnchorName;
    private boolean mIsAnchor = false;

    public FansHomeView(Context context) {
        this(context, null);
    }

    public FansHomeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FansHomeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.fans_home_view, this);

        mEmptyView = $(R.id.empty_view);
        mMainArea = $(R.id.all_area);
        mMainArea.setVisibility(View.GONE);

        mMyInfoContainer = $(R.id.vfan_myinfo);

        mDetailBasicView = $(R.id.detail_basic_view);

        mJoinBannerIv = $(R.id.join_banner_iv);
        mMyInfoArea = $(R.id.my_info_area);
        mMyMedalTv = $(R.id.my_medal_tv);
        mFanRankTv = $(R.id.vfan_rank_tv);
        mFanValueTv = $(R.id.vfan_value_tv);

        mPrivilegeTitleTv = $(R.id.privilege_title_tv);
        mAcceleratePrivilegeIv = $(R.id.accelerate_privilege_iv);
        mAcceleratePrivilegeTitleTv = $(R.id.accelerate_privilege_title_tv);
        mAcceleratePrivilegeStatusTv = $(R.id.accelerate_status);
        mColorBarrageIv = $(R.id.color_barrage_iv);
        mColorBarrageTitleTv = $(R.id.color_barrage_title_tv);
        mColorBarrageStatusTv = $(R.id.color_barrage_status);
        mFlyBarrageIv = $(R.id.fly_barrage_iv);
        mFlyBarrageTitleTv = $(R.id.fly_barrage_title_tv);
        mFlyBarrageStatusTv = $(R.id.fly_barrage_status);
        mForbiddenIv = $(R.id.forbidden_iv);
        mForbiddenTitleTv = $(R.id.forbidden_title_tv);
        mForbiddenStatusTv = $(R.id.forbidden_status);

        $click(R.id.first_privilege_area, this);
        $click(R.id.color_barrage_area, this);
        $click(R.id.fly_barrage_privilege_area, this);
        $click(R.id.forbidden_privilege_area, this);
        $click(R.id.group_rank_area, this);

        initPresenter();
    }

    private void initPresenter() {
        mPresenter = new FansHomePresenter(this);
    }

    public void setData(String anchorName, @NonNull FansGroupDetailModel groupDetailModel) {
        mAnchorName = anchorName;
        mGroupDetailModel = groupDetailModel;

        if (mGroupDetailModel != null) {
            refresh();
            mPresenter.getTopThreeMember(mGroupDetailModel.getZuid());
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private void refresh() {
        mEmptyView.setVisibility(GONE);
        mMainArea.setVisibility(View.VISIBLE);

        String anchorName = mAnchorName;
        if (!TextUtils.isEmpty(anchorName) && anchorName.length() > 6) {
            anchorName = anchorName.substring(0, 6);
        }
        mIsAnchor = mGroupDetailModel.getZuid() == UserAccountManager.getInstance().getUuidAsLong();
        mMyInfoArea.setVisibility(mIsAnchor ? View.GONE : View.VISIBLE);

        mDetailBasicView.setGroupDetailModel(mGroupDetailModel, anchorName);

        if (!mIsAnchor) {
            if (mGroupDetailModel.getMemType() == VFansCommonProto.GroupMemType.NONE_VALUE) {
                mMyInfoArea.setVisibility(GONE);
                mJoinBannerIv.setVisibility(VISIBLE);
            } else {
                mMyInfoArea.setVisibility(VISIBLE);
                mJoinBannerIv.setVisibility(GONE);

                mMyMedalTv.setText(mGroupDetailModel.getMedalValue());
                mMyMedalTv.setBackgroundResource(FansInfoUtils.getGroupMemberLevelDrawable(
                        mGroupDetailModel.getMyPetLevel()));
                mFanValueTv.setText(String.valueOf(mGroupDetailModel.getMyPetExp()));
                mFanRankTv.setText(mGroupDetailModel.getPetRanking() + "/"
                        + mGroupDetailModel.getCurrentMember());
            }
            if (hasPrivilege()) {
                int myPetLevel = mGroupDetailModel.getMyPetLevel();
                if (myPetLevel > UPGRADE_ACCELERATE_LEVEL) {
                    updateLevelTv(mAcceleratePrivilegeStatusTv);
                }
                if (myPetLevel > SEND_COLOR_BARRAGE_VIP_LEVEL) {
                    updateLevelTv(mColorBarrageStatusTv);
                }
                if (myPetLevel > SEND_FLY_BARRAGE_LEVEL) {
                    updateLevelTv(mColorBarrageStatusTv);
                }
                if (myPetLevel > BAN_LEVEL) {
                    updateLevelTv(mForbiddenStatusTv);
                }
            }
        } else {
            mMyInfoContainer.setVisibility(GONE);
            mPrivilegeTitleTv.setText(R.string.my_privilege_title);

            mAcceleratePrivilegeTitleTv.setText(R.string.vfans_privilege_chram_title);
            mAcceleratePrivilegeIv.setImageResource(R.drawable.live_pet_group_charm_title);
            mAcceleratePrivilegeStatusTv.setVisibility(GONE);

            mColorBarrageTitleTv.setText(R.string.vfans_privilege_tour_divide);
            mColorBarrageIv.setImageResource(R.drawable.live_pet_group_tour_divide);
            mColorBarrageStatusTv.setVisibility(GONE);

            mFlyBarrageIv.setImageResource(R.drawable.live_pet_group_more_fans);
            mFlyBarrageTitleTv.setText(R.string.vfans_privilege_more_fans);
            mFlyBarrageStatusTv.setVisibility(GONE);

            mForbiddenIv.setImageResource(R.drawable.live_pet_group_comingsoon);
            mForbiddenTitleTv.setText(R.string.vfans_privilege_comingsoon);
            mForbiddenStatusTv.setVisibility(GONE);
        }
    }

    private void updateLevelTv(TextView tv) {
        Drawable drawable = GlobalData.app().getResources().getDrawable(R.drawable.live_pet_group_have_turned);
        drawable.setBounds(0, 0, DisplayUtils.dip2px(9.33f), DisplayUtils.dip2px(9.33f));
        tv.setCompoundDrawables(drawable, null, null, null);
        tv.setText(R.string.vfans_privilege_has_open);
    }

    public boolean hasPrivilege() {
        return mGroupDetailModel.getVipLevel() > 0 &&
                System.currentTimeMillis() / 1000 < mGroupDetailModel.getVipExpire();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.first_privilege_area
                || i == R.id.color_barrage_area
                || i == R.id.fly_barrage_privilege_area
                || i == R.id.forbidden_privilege_area) {
            int type = getType(i);
            if (type != -1) {
                FansPrivilegeFragment.openFragment((BaseSdkActivity) getContext(), type);
            }
        } else if (i == R.id.group_rank_area) {

        }
    }

    private int getType(@IdRes int resId) {
        if (resId == R.id.first_privilege_area) {
            return mIsAnchor ? TYPE_CHARM_MEDAL : TYPE_UPGRADE_ACCELERATION;
        } else if (resId == R.id.fly_barrage_privilege_area) {
            return mIsAnchor ? TYPE_MORE_FANS : TYPE_FREE_FLY_BARRAGE;
        } else if (resId == R.id.color_barrage_area) {
            return mIsAnchor ? TYPE_TOUR_DIVIDE : TYPE_COLOR_BARRAGE;
        } else if (resId == R.id.forbidden_privilege_area) {
            return mIsAnchor ? -1 : TYPE_BAN_BARRAGE;
        }
        return -1;
    }

    @Override
    public void setTopThreeMember(List<FansMemberModel> memberList) {
        mDetailBasicView.setTopThreeMember(memberList);
    }
}
