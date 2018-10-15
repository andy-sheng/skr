package rank.holder;

import android.view.View;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import rank.model.ContestRankItemModel;
import util.FormatUtils;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

/**
 * Created by lan on 2018/1/11.
 */
public class ContestRankHolder extends BaseHolder<ContestRankItemModel> {
    private TextView mIndexTv;
    private BaseImageView mAvatarIv;
    private TextView mNameTv;
    private TextView mBonusTv;

    public ContestRankHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        mIndexTv = $(R.id.index_tv);
        mAvatarIv = $(R.id.avatar_iv);
        mNameTv = $(R.id.name_tv);
        mBonusTv = $(R.id.bonus_tv);
    }

    @Override
    protected void bindView() {
        mIndexTv.setText(String.valueOf(mViewModel.getIndex()));

        float myBonus = mViewModel.getMyBonus();
        mBonusTv.setText(FormatUtils.formatMoney(myBonus));

        User user = mViewModel.getUser();
        AvatarUtils.loadAvatarByUidTs(mAvatarIv, user.getUid(), user.getAvatar(), true);

        mNameTv.setText(user.getNickname());
    }
}
