package rank.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import rank.model.ContestRankItemModel;
import util.FormatUtils;

/**
 * Created by lan on 2018/1/11.
 */
public class ContestRankTopThreeHolder extends BaseArrayHolder<ContestRankItemModel> {
    private int[] mRankAreaIds;
    private int[] mAvatarIds;
    private int[] mNameIds;
    private int[] mBonusIds;

    private ViewGroup[] mRankArea;
    private BaseImageView[] mAvatarIv;
    private TextView[] mNameTv;
    private TextView[] mBonusTv;

    public ContestRankTopThreeHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        mRankAreaIds = new int[]{
                R.id.rank_area1, R.id.rank_area2, R.id.rank_area3,
        };
        mAvatarIds = new int[]{
                R.id.avatar_iv1, R.id.avatar_iv2, R.id.avatar_iv3,
        };
        mNameIds = new int[]{
                R.id.name_tv1, R.id.name_tv2, R.id.name_tv3,
        };
        mBonusIds = new int[]{
                R.id.bonus_tv1, R.id.bonus_tv2, R.id.bonus_tv3,
        };

        mRankArea = new ViewGroup[3];
        mAvatarIv = new BaseImageView[3];
        mNameTv = new TextView[3];
        mBonusTv = new TextView[3];

        for (int i = 0; i < 3; i++) {
            mRankArea[i] = $(mRankAreaIds[i]);
            mAvatarIv[i] = $(mAvatarIds[i]);
            mNameTv[i] = $(mNameIds[i]);
            mBonusTv[i] = $(mBonusIds[i]);
        }
    }

    @Override
    protected void bindView() {
        for (int i = 0; i < 3; i++) {
            if (i < mViewModels.size()) {
                bindItem(i, mAvatarIv[i], mNameTv[i], mBonusTv[i]);
                mRankArea[i].setVisibility(View.VISIBLE);
            } else {
                mRankArea[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    private void bindItem(int index, BaseImageView avatarIv, TextView nameTv, TextView bonusTv) {
        float myBonus = mViewModels.get(index).getMyBonus();
        bonusTv.setText(FormatUtils.formatMoney(myBonus));

        User user = mViewModels.get(index).getUser();
        AvatarUtils.loadAvatarByUidTs(avatarIv, user.getUid(), user.getAvatar(), true);
        nameTv.setText(user.getNickname());
    }
}
