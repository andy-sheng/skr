package com.module.rankingmode.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.flowlayout.FlowLayout;
import com.common.flowlayout.TagAdapter;
import com.common.flowlayout.TagFlowLayout;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.rankingmode.R;

// TODO: 2018/12/25 先放在该层，完善在换
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

    private PersonInfoDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private PersonInfoDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
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
