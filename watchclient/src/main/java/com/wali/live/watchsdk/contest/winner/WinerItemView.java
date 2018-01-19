package com.wali.live.watchsdk.contest.winner;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.contest.model.AwardUser;

/**
 * Created by jiyangli on 18-1-15.
 */
public class WinerItemView extends LinearLayout {
    SimpleDraweeView imgAvatar;
    TextView txtName;
    TextView txtReward;

    public WinerItemView(Context context) {
        super(context);
        init(context);
    }

    public WinerItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WinerItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.winer_item_layout, null);
        imgAvatar = (SimpleDraweeView) view.findViewById(R.id.winer_item_imgAvatar);
        txtName = (TextView) view.findViewById(R.id.winer_item_txtName);
        txtReward = (TextView) view.findViewById(R.id.winer_item_txtReward);
        addView(view);
    }

    public void setData(AwardUser awardUser, float award) {
        AvatarUtils.loadAvatarByUidTs(imgAvatar, awardUser.getUuid(), awardUser.getAvatar(), true);
        txtReward.setText("¥ " + String.valueOf(award));
        txtName.setText(awardUser.getNickName());
    }
}
