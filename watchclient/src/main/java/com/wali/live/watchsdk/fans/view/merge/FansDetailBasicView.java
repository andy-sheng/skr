package com.wali.live.watchsdk.fans.view.merge;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.HttpImage;
import com.base.image.fresco.image.ResImage;
import com.base.mvp.specific.RxRelativeLayout;
import com.base.utils.display.DisplayUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;
import com.wali.live.common.barrage.view.utils.FansInfoUtils;
import com.wali.live.watchsdk.fans.view.custom.FansProgressView;

import java.util.List;

/**
 * Created by lan on 2017/11/16.
 */
public class FansDetailBasicView extends RxRelativeLayout {
    private static final int MAX_COUNT_TOP = 3;

    private TextView mFanTitleTv;
    private BaseImageView mCoverIv;
    private TextView mFansNameTv;
    private ImageView mCharmTitleIv;
    private TextView mLevelTv;

    private FansProgressView mCharmPv;

    private TextView mMemberCountTv;
    private TextView mGroupRankTv;

    private LinearLayout mFansListArea;

    private FansGroupDetailModel mGroupDetailModel;
    private String mAnchorName;

    public FansDetailBasicView(Context context) {
        super(context);
        init();
    }

    public FansDetailBasicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FansDetailBasicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.fans_detail_basic_view, this);

        mFanTitleTv = $(R.id.vfan_owner_title);
        mCoverIv = $(R.id.cover_iv);
        mFansNameTv = $(R.id.vfan_name_tv);
        mCharmTitleIv = $(R.id.charm_title_iv);
        mLevelTv = $(R.id.level_tv);

        mCharmPv = $(R.id.charm_pv);

        mMemberCountTv = $(R.id.member_count_tv);
        mFansListArea = $(R.id.vfans_list_area);
        mGroupRankTv = $(R.id.group_rank_tv);
    }

    public void setGroupDetailModel(FansGroupDetailModel groupDetailModel) {
        setGroupDetailModel(groupDetailModel, "");
    }

    public void setGroupDetailModel(FansGroupDetailModel groupDetailModel, String anchorName) {
        mGroupDetailModel = groupDetailModel;
        mAnchorName = anchorName;

        updateDetailView();
    }

    private void updateDetailView() {
        if (!TextUtils.isEmpty(mAnchorName)) {
            mFanTitleTv.setText(getContext().getString(R.string.vfans_owner_name, mAnchorName));
            mFanTitleTv.setVisibility(View.VISIBLE);
        } else {
            mFanTitleTv.setVisibility(View.GONE);
        }

        mGroupRankTv.setText(String.valueOf(mGroupDetailModel.getRanking()));
        mFansNameTv.setText(mGroupDetailModel.getGroupName());
        mCharmTitleIv.setImageResource(
                FansInfoUtils.getImageResourcesByCharmLevelValue(mGroupDetailModel.getCharmLevel()));

        mLevelTv.setText("Lv." + mGroupDetailModel.getCharmLevel());
        mLevelTv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        mCharmPv.setProgress(mGroupDetailModel.getCharmExp(), mGroupDetailModel.getNextCharmExp());
        mMemberCountTv.setText(String.valueOf(mGroupDetailModel.getCurrentMember()));

        AvatarUtils.loadAvatarByUidTs(mCoverIv, mGroupDetailModel.getZuid(), 0, true);
    }

    public void setTopThreeMember(List<FansMemberModel> memberList) {
        mFansListArea.removeAllViews();

        int memberCount = memberList == null ? 0 : memberList.size();
        for (int i = 0; i < MAX_COUNT_TOP; i++) {
            BaseImageView iv = new BaseImageView(this.getContext());
            mFansListArea.addView(iv);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) iv.getLayoutParams();
            lp.width = DisplayUtils.dip2px(18);
            lp.height = DisplayUtils.dip2px(18);
            if (i > 0) {
                lp.leftMargin = DisplayUtils.dip2px(8);
            }

            if (i < memberCount) {
                addTopThreeImage(iv, memberList.get(i));
            } else {
                addPlaceHolderImage(iv);
            }
        }
    }

    private void addTopThreeImage(BaseImageView iv, FansMemberModel memberInfo) {
        HttpImage image = new HttpImage(AvatarUtils.getAvatarUrlByUid(memberInfo.getUuid(), memberInfo.getAvatar()));
        image.setHeight(DisplayUtils.dip2px(18));
        image.setWidth(DisplayUtils.dip2px(18));
        image.setLoadingDrawable(GlobalData.app().getResources().getDrawable(R.drawable.avatar_default_a));
        image.setFailureDrawable(GlobalData.app().getResources().getDrawable(R.drawable.avatar_default_a));
        image.setIsCircle(true);
        FrescoWorker.loadImage(iv, image);
    }

    private void addPlaceHolderImage(BaseImageView iv) {
        ResImage image = new ResImage(R.drawable.pet_group_placeholder);
        image.setHeight(DisplayUtils.dip2px(18));
        image.setWidth(DisplayUtils.dip2px(18));
        image.setIsCircle(true);
        FrescoWorker.loadImage(iv, image);
    }
}
