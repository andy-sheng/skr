package com.wali.live.watchsdk.fans.holder;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.image.fresco.BaseImageView;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.MyGroupDetailFragment;
import com.wali.live.watchsdk.fans.model.item.MyFansGroupModel;
import com.wali.live.watchsdk.fans.utils.FansInfoUtils;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

/**
 * Created by lan on 2017/11/15.
 */
public class MyFansGroupHolder extends BaseHolder<MyFansGroupModel> {
    private BaseImageView avatarIv;

    private TextView nameTv;
    private ImageView titleIv;

    private TextView expValueTv;
    private TextView rankingTv;

    public MyFansGroupHolder(View itemView) {
        super(itemView);

    }

    @Override
    protected void initView() {
        avatarIv = $(R.id.avatar);
        nameTv = $(R.id.name_tv);
        titleIv = $(R.id.charm_title_iv);
        expValueTv = $(R.id.exp_value_tv);
        rankingTv = $(R.id.ranking_tv);

        initListener();
    }

    private void initListener() {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyGroupDetailFragment.open((BaseActivity) itemView.getContext());
            }
        });
    }

    @Override
    protected void bindView() {
        titleIv.setImageResource(FansInfoUtils.getImageResourcesByCharmLevelValue(mViewModel.getCharmLevel()));

        AvatarUtils.loadAvatarByUid(avatarIv, UserAccountManager.getInstance().getUuidAsLong(), true);
        nameTv.setText(mViewModel.getGroupName());

        String hint = itemView.getContext().getString(R.string.meili_value) + ": ";
        SpannableString ss = new SpannableString(hint + String.valueOf(mViewModel.getCharmExp()));
        ss.setSpan(new ForegroundColorSpan(itemView.getContext().getResources().getColor(R.color.color_ff2966)), hint.length() - 1, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        expValueTv.setText(ss);

        String rankHint = itemView.getContext().getString(R.string.group_ranking) + ": ";
        SpannableString rankSpan = new SpannableString(rankHint + mViewModel.getRanking());
        rankSpan.setSpan(new ForegroundColorSpan(itemView.getContext().getResources().getColor(R.color.color_ff2966)), rankHint.length() - 1, rankSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        rankingTv.setText(rankSpan);

    }
}
