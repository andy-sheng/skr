package com.wali.live.watchsdk.contest.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.dialog.DialogUtils;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.image.fresco.BaseImageView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.contest.cache.ContestGlobalCache;
import com.wali.live.watchsdk.contest.util.FormatUtils;
import com.wali.live.watchsdk.contest.view.ContestRevivalRuleView;

/**
 * Created by jiang on 18-1-13.
 */
public class MyContestInfoFragment extends BaseFragment implements View.OnClickListener {
    private ImageView mBackBtn;
    private TextView mNameTv;

    private BaseImageView mAvatarIv;

    private TextView mIncomeTv;
    private TextView mRankTv;

    private TextView mWithdrawBtn;

    private TextView mRevivalCountTv;
    private TextView mInviteBtn;

    private ContestRevivalRuleView mRevivalRuleView;

    private User mMySelf;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_my_contest_info, container, false);
    }

    @Override
    protected void bindView() {
        mMySelf = MyUserInfoManager.getInstance().getUser();

        mBackBtn = $(R.id.back_btn);
        mBackBtn.setOnClickListener(this);

        mNameTv = $(R.id.name_tv);

        mAvatarIv = $(R.id.avatar_iv);

        mIncomeTv = $(R.id.income_tv);
        mRankTv = $(R.id.rank_tv);

        mWithdrawBtn = $(R.id.withdraw_btn);
        mWithdrawBtn.setOnClickListener(this);

        mRevivalCountTv = $(R.id.revival_count_tv);

        mInviteBtn = $(R.id.invite_tv);
        mInviteBtn.setOnClickListener(this);

        mRevivalRuleView = $(R.id.revival_rule_view);

        updateView();
    }

    private void updateView() {
        updateMyInfoView();
        updateMyContestView();

        mRevivalCountTv.setText(String.valueOf(ContestGlobalCache.getRevivalNum()));
    }

    private void updateMyInfoView() {
        AvatarUtils.loadAvatarByUidTs(mAvatarIv, mMySelf.getUid(), mMySelf.getAvatar(), true);

        mNameTv.setText(mMySelf.getNickname());
    }

    private void updateMyContestView() {
        float totalIncome = ContestGlobalCache.getTotalIncome();
        mIncomeTv.setText(FormatUtils.formatMoney(totalIncome));

        int rank = ContestGlobalCache.getRank();
        if (rank <= 0 || rank > 100) {
            mRankTv.setText("-");
        } else {
            mRankTv.setText(String.valueOf(rank));
        }
    }

    @Override
    public int getRequestCode() {
        return 0;
    }

    private void showRevivalRuleView() {
        mRevivalRuleView.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_btn) {
            finish();
        } else if (id == R.id.withdraw_btn) {
            enterMyIncome();
        } else if (id == R.id.invite_tv || id == R.id.share_btn || id == R.id.input_share_btn) {
            showRevivalRuleView();
        }
    }

    private void enterMyIncome() {
//        Intent intent = new Intent(getActivity(), UserIncomeActivity.class);
//        getActivity().startActivity(intent);
        showDialog();

    }

    private void showDialog() {
        DialogUtils.showNormalDialog(getActivity(),
                "",
                getString(R.string.income_content), R.string.income_download, R.string.income_think,
                new DialogUtils.IDialogCallback() {
                    @Override
                    public void process(DialogInterface dialogInterface, int i) {
                        //走走浏览器下载 打开应用商店
                        Uri uri = Uri.parse("http://app.mi.com/details?id=com.wali.live");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                }, null);
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    public static void openFragment(BaseActivity activity) {
        FragmentNaviUtils.addFragment(activity, R.id.main_act_container, MyContestInfoFragment.class, null, true, true, true);
    }
}
