package com.zq.dialog;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.flowlayout.FlowLayout;
import com.common.flowlayout.TagAdapter;
import com.common.flowlayout.TagFlowLayout;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;

// 个人信息卡片view
public class PersonInfoDialogView extends RelativeLayout {

    private String[] mVals = new String[]{"回龙观情歌榜22位", "铂金唱将", "北京/昌平", "22岁"};

    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ExTextView mSignTv;
    ExTextView mLevelTv;
    ExTextView mReport;
    TagFlowLayout mFlowlayout;
    ExTextView mFollowTv;

    public PersonInfoDialogView(Context context) {
        super(context);
        init();
    }

    public PersonInfoDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PersonInfoDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.person_info_dialog_view, this);

        mAvatarIv = (SimpleDraweeView) this.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) this.findViewById(R.id.name_tv);
        mSignTv = (ExTextView) this.findViewById(R.id.sign_tv);
        mLevelTv = (ExTextView) this.findViewById(R.id.level_tv);
        mReport = (ExTextView) this.findViewById(R.id.report);
        mFlowlayout = (TagFlowLayout) this.findViewById(R.id.flowlayout);
        mFollowTv = (ExTextView) this.findViewById(R.id.follow_tv);

        // TODO: 2018/12/26 暂时砍掉举报 
        mReport.setVisibility(GONE);
    }

    public void setData(UserInfoModel userInfo){
        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(userInfo.getAvatar())
                        .setCircle(true)
                        .setBorderWidth(2)
                        .setBorderColor(Color.BLUE)
                        .build());
        mNameTv.setText(userInfo.getNickname());
        mSignTv.setText(userInfo.getSignature());

        // TODO: 2018/12/26  根据产品策略调整
        mFlowlayout.setAdapter(new TagAdapter<String>(mVals) {
            @Override
            public View getView(FlowLayout parent, int position, String o) {
                ExTextView tv = (ExTextView) LayoutInflater.from(getContext()).inflate(R.layout.tag_textview,
                        mFlowlayout, false);
                tv.setText(o);
                return tv;
            }
        });
    }

}
