package rank;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.activity.BaseSdkActivity;
import com.base.image.fresco.BaseImageView;
import com.base.view.BackTitleBar;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.contest.cache.ContestGlobalCache;
import rank.adapter.ContestRankAdapter;
import rank.model.ContestRankModel;
import rank.presenter.ContestRankPresenter;
import rank.presenter.IContestRankView;
import util.FormatUtils;

/**
 * Created by lan on 2018/1/11.
 */
public class ContestRankActivity extends BaseSdkActivity implements View.OnClickListener, IContestRankView {
    private BackTitleBar mTitleBar;

    private RecyclerView mRankRv;
    private ContestRankAdapter mRankAdapter;

    private TextView mIndexTv;
    private BaseImageView mAvatarIv;
    private TextView mNameTv;
    private TextView mBonusTv;

    private ContestRankPresenter mRankPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contest_rank_layout);

        initView();
        initPresenter();
    }

    private void initView() {
        initTitleView();
        initRankView();
        initMyView();
    }

    private void initTitleView() {
        mTitleBar = $(R.id.title_bar);
        mTitleBar.getBackBtn().setOnClickListener(this);

        mTitleBar.setBackgroundColor(getResources().getColor(R.color.color_contest_theme));
        mTitleBar.setCenterTitleText(R.string.contest_prepare_total_rank);
        mTitleBar.showCenterTitle();
        mTitleBar.hideBottomLine();

        mTitleBar.adjustToLightMode();
//        mTitleBar.getRightImageBtn().setImageResource(R.drawable.live_icon_share_btn);
//        mTitleBar.getRightImageBtn().setOnClickListener(this);
    }

    private void initRankView() {
        mRankRv = $(R.id.recycler_view);
        mRankRv.setLayoutManager(new LinearLayoutManager(this));

        mRankAdapter = new ContestRankAdapter();
        mRankRv.setAdapter(mRankAdapter);

//        List<ContestRankItemModel> list = new ArrayList();
//        for (int i = 0; i < 10; i++) {
//            list.add(ContestRankItemModel.newTestInstance(i));
//        }
//        mRankAdapter.setDataList(list);
    }

    private void initMyView() {
        mIndexTv = $(R.id.index_tv);
        mAvatarIv = $(R.id.avatar_iv);
        mNameTv = $(R.id.name_tv);
        mBonusTv = $(R.id.bonus_tv);

        int rank = ContestGlobalCache.getRank();
        mIndexTv.setText(FormatUtils.formatRank(rank));

        float totalIncome = ContestGlobalCache.getTotalIncome();
        mBonusTv.setText(FormatUtils.formatMoney(totalIncome));

        User user = MyUserInfoManager.getInstance().getUser();
        AvatarUtils.loadAvatarByUidTs(mAvatarIv, user.getUid(), user.getAvatar(), true);
        mNameTv.setText(user.getNickname());
    }

    private void initPresenter() {
        mRankPresenter = new ContestRankPresenter(this);
        mRankPresenter.getContestRank();
    }

    @Override
    public void setContestRank(ContestRankModel model) {
        mRankAdapter.setDataList(model.getItemList());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_iv) {
            finish();
        }
    }

    public static void open(BaseActivity activity) {
        Intent intent = new Intent(activity, ContestRankActivity.class);
        activity.startActivity(intent);
    }
}
