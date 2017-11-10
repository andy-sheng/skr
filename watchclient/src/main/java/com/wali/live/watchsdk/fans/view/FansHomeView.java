package com.wali.live.watchsdk.fans.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.HttpImage;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.utils.FansInfoUtils;
import com.wali.live.watchsdk.view.EmptyView;

/**
 * Created by zyh on 2017/11/8.
 *
 * @module 粉丝团的首页
 */
public class FansHomeView extends RelativeLayout implements View.OnClickListener {
    private final String TAG = "FansHomeView";
    public static final int COVER_IV_WIDTH = DisplayUtils.dip2px(40);
    private String mAnchorName;
    private FansGroupDetailModel mGroupDetailModel;
    private EmptyView mEmptyView;
    private TextView mFanTitleTv;

    private TextView mFanLevelTv;
    private FansProgressView mFansProgressView;

    private TextView mMemberCntTv;
    //粉丝排行榜
    private ViewGroup mFansRankContainer;
    private LinearLayout mFansRankListArea;
    //群排行榜
    private ViewGroup mGroupRankContainer;
    private TextView mGroupRankTv;

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
    private TextView mColorBarrageStatus;
    //飞屏特权
    private ImageView mFlyBarrageIv;
    private TextView mFlyBarrageTitleTv;
    private TextView mFlyBarrageStatus;
    //禁言特权
    private ImageView mForbiddenIv;
    private TextView mForbiddenTitleTv; //禁言特权
    private TextView mForbiddenStatus; //禁言特权

    public void setGroupDetailModel(String anchorName, @NonNull FansGroupDetailModel groupDetailModel) {
        mAnchorName = anchorName;
        mGroupDetailModel = groupDetailModel;
        if (mGroupDetailModel != null) {
            refresh();
        }
    }

    private void refresh() {
        mEmptyView.setVisibility(GONE);
        String anchorName = mAnchorName;
        if (!TextUtils.isEmpty(anchorName) && anchorName.length() > 6) {
            anchorName = anchorName.substring(0, 6);
        }
        $(R.id.vfan_myinfo).setVisibility(mGroupDetailModel.getZuid()
                == UserAccountManager.getInstance().getUuidAsLong() ? View.GONE : View.VISIBLE);
        ((TextView) $(R.id.vfan_owner_title)).setText(String.format(GlobalData.app().getResources()
                .getString(R.string.vfans_owner_name), anchorName));
        ((TextView) $(R.id.group_rank_tv)).setText(mGroupDetailModel.getRanking());
        ((TextView) $(R.id.vfan_name_tv)).setText(mGroupDetailModel.getGroupName());
        ((ImageView) $(R.id.charm_title_iv)).setImageResource(
                FansInfoUtils.getImageResoucesByCharmLevelValue(mGroupDetailModel.getCharmLevel()));

        mFanLevelTv.setText("Lv." + mGroupDetailModel.getCharmLevel());
        mFanLevelTv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        mFansProgressView.setProgress(mGroupDetailModel.getCharmExp(), mGroupDetailModel.getNextCharmExp());
        mMemberCntTv.setText(mGroupDetailModel.getCurrentMember());

        HttpImage coverHttpImage = new HttpImage(AvatarUtils.getAvatarUrlByUid(mGroupDetailModel.getZuid(), 0));
        coverHttpImage.setIsCircle(true);
        coverHttpImage.setWidth(COVER_IV_WIDTH);
        coverHttpImage.setHeight(COVER_IV_WIDTH);
        coverHttpImage.setLoadingDrawable(GlobalData.app().getResources().getDrawable(R.drawable.avatar_default_a));
        coverHttpImage.setFailureDrawable(GlobalData.app().getResources().getDrawable(R.drawable.avatar_default_a));
        FrescoWorker.loadImage(((BaseImageView) $(R.id.cover_iv)), coverHttpImage);

        if (mGroupDetailModel.getZuid() == UserAccountManager.getInstance().getUuidAsLong()) {
            //主播本人
        } else {
            mPrivilegeTitleTv.setText(R.string.my_privilege_title);
            mAcceleratePrivilegeTitleTv.setText(R.string.vfans_privilege_chram_title);
            mAcceleratePrivilegeIv.setImageResource(R.drawable.live_pet_group_charm_title);
            mAcceleratePrivilegeStatusTv.setVisibility(GONE);

            mColorBarrageTitleTv.setText(R.string.vfans_privilege_tour_divide);
            mColorBarrageIv.setImageResource(R.drawable.live_pet_group_tour_divide);
            mColorBarrageStatus.setVisibility(GONE);

            mFlyBarrageIv.setImageResource(R.drawable.live_pet_group_more_fans);
            mFlyBarrageTitleTv.setText(R.string.vfans_privilege_more_fans);
            mFlyBarrageStatus.setVisibility(GONE);

            mForbiddenIv.setImageResource(R.drawable.live_pet_group_comingsoon);
            mForbiddenTitleTv.setText(R.string.vfans_privilege_comingsoon);
            mForbiddenStatus.setVisibility(GONE);
        }
    }

    private final <V extends View> V $(@IdRes int id) {
        return (V) findViewById(id);
    }

    private final <V extends View> void $click(@IdRes int id, OnClickListener listener) {
        V view = $(id);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    private final <V extends View> void $click(V view, OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    public FansHomeView(Context context) {
        this(context, null);
    }

    public FansHomeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FansHomeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.vfans_info, this);
        initView(context);
    }

    private void initView(Context context) {
        mEmptyView = $(R.id.empty_view);
        $click(mEmptyView, this);

        mFanLevelTv = $(R.id.level_tv);
        mFansProgressView = $(R.id.charm_progress);

        mMemberCntTv = $(R.id.member_count_tv);
        mFansRankListArea = $(R.id.vfans_list_area);

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
        mColorBarrageStatus = $(R.id.color_barrage_status);

        mFlyBarrageIv = $(R.id.fly_barrage_iv);
        mFlyBarrageTitleTv = $(R.id.fly_barrage_title_tv);
        mFlyBarrageStatus = $(R.id.fly_barrage_status);

        mForbiddenIv = $(R.id.forbidden_iv);
        mForbiddenTitleTv = $(R.id.forbidden_title_tv);
        mForbiddenStatus = $(R.id.forbidden_status);

        $click(R.id.vfan_recommend, this);
        $click(R.id.first_privilege_area, this);
        $click(R.id.color_barrage_area, this);
        $click(R.id.fly_barrage_privilege_area, this);
        $click(R.id.forbidden_privilege_area, this);
        $click(R.id.group_rank_area, this);
        $click(R.id.vfans_rank_area, this);
        $click(R.id.group_member_area, this);

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.first_privilege_area) {
            //跳转粉丝特权页面
        } else if (i == R.id.color_barrage_area) {

        } else if (i == R.id.fly_barrage_privilege_area) {

        } else if (i == R.id.forbidden_privilege_area) {

        } else if (i == R.id.forbidden_privilege_area) {

        } else if (i == R.id.group_rank_area) {

        } else if (i == R.id.vfans_rank_area) {

        } else if (i == R.id.group_member_area) {

        }
    }
}
