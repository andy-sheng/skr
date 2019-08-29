package com.module.home.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.component.person.model.UserRankModel;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.home.R;
import com.component.level.utils.LevelConfigUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInfoTitleView2 extends RelativeLayout {

    RelativeLayout mContentArea;
    SimpleDraweeView mIvUserIcon;
    ImageView mLevelBg;
    ExTextView mUserLevelTv;
    ExTextView mNameTv;
    ExTextView mRankInfo;

    public UserInfoTitleView2(Context context) {
        super(context);
        init();
    }

    public UserInfoTitleView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UserInfoTitleView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.user_info_title2_layout, this);

        mContentArea = (RelativeLayout) this.findViewById(R.id.content_area);
        mIvUserIcon = (SimpleDraweeView) this.findViewById(R.id.iv_user_icon);
        mLevelBg = (ImageView) this.findViewById(R.id.level_bg);
        mUserLevelTv = (ExTextView) this.findViewById(R.id.user_level_tv);
        mNameTv = (ExTextView) this.findViewById(R.id.name_tv);
        mRankInfo = (ExTextView) this.findViewById(R.id.rank_info);
        showBaseInfo();
    }

    public void showBaseInfo() {
        AvatarUtils.loadAvatarByUrl(mIvUserIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .build());
    }

    public void showRankView(UserRankModel userRankModel) {
        mRankInfo.setText(highlight(userRankModel.getText(), userRankModel.getHighlight()));
        if (LevelConfigUtils.getAvatarLevelBg(userRankModel.getMainRanking()) != 0) {
            mLevelBg.setBackground(getResources().getDrawable(LevelConfigUtils.getAvatarLevelBg(userRankModel.getMainRanking())));
        }

        if (LevelConfigUtils.getHomePageTopBg(userRankModel.getMainRanking()) != null) {
            mContentArea.setBackground(LevelConfigUtils.getHomePageTopBg(userRankModel.getMainRanking()));
        }
        if (userRankModel.getSubRanking() == 0) {
            mNameTv.setText(userRankModel.getMainDesc());
        } else {
            mNameTv.setText(userRankModel.getMainDesc() + " " + userRankModel.getSubRanking() + "段");
        }
        mUserLevelTv.setTextColor(Color.parseColor(LevelConfigUtils.getHomePageLevelTextColor(userRankModel.getMainRanking())));
        mUserLevelTv.setText(userRankModel.getMainDesc());
    }

    private SpannableString highlight(String text, String target) {
        SpannableString spannableString = new SpannableString(text);
        Pattern pattern = Pattern.compile(target);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            ForegroundColorSpan span = new ForegroundColorSpan(Color.parseColor("#FFC484"));
            spannableString.setSpan(span, matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

}
