package com.wali.live.watchsdk.longtext;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.base.activity.BaseSdkActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.longtext.adapter.FeedItemUiType;
import com.wali.live.watchsdk.longtext.adapter.LongTextAdapter;
import com.wali.live.watchsdk.longtext.model.LongTextModel;
import com.wali.live.watchsdk.longtext.model.interior.item.BaseFeedItemModel;
import com.wali.live.watchsdk.longtext.presenter.ILongTextView;
import com.wali.live.watchsdk.longtext.presenter.LongTextPresenter;

import java.util.List;

import rx.Observable;

/**
 * Created by lan on 2017/9/19.
 */
public class LongTextActivity extends BaseSdkActivity implements ILongTextView {
    private static final String EXTRA_FEED_ID = "extra_feed_id";
    private static final String EXTRA_OWNER_ID = "extra_owner_id";

    private BackTitleBar mBgBar;
    private BackTitleBar mTitleBar;

    private RecyclerView mContentRv;
    private LinearLayoutManager mLayoutManager;
    private LongTextAdapter mAdapter;

    private LongTextPresenter mPresenter;

    private String mFeedId;
    private long mOwnerId;

    private int mScrollY;
    private int mTopHeight;
    private boolean mIsDark = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_longtext);

        initData();
        initView();
        initPresenter();
    }

    private void initData() {
        Intent data = getIntent();
        if (data == null) {
            MyLog.e(TAG, "data is null");
            finish();
            return;
        }
        mFeedId = data.getStringExtra(EXTRA_FEED_ID);
        mOwnerId = data.getLongExtra(EXTRA_OWNER_ID, 0);
    }

    private void initView() {
        mBgBar = $(R.id.bg_bar);
        mBgBar.setBackgroundResource(R.drawable.zhezhao_video_statusbar);
        mBgBar.getTitleTv().setTextColor(getResources().getColor(R.color.color_white_trans_80));
        mBgBar.getBackBtn().setCompoundDrawablesWithIntrinsicBounds(R.drawable.topbar_icon_all_back_light_normal, 0, 0, 0);
        mBgBar.setTitle(R.string.detail_text);
        mBgBar.hideBottomLine();
        mBgBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTitleBar = $(R.id.title_bar);
        mTitleBar.setAlpha(0f);
        mTitleBar.setTitle(R.string.detail_text);
        mTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mContentRv = $(R.id.content_rv);

        mLayoutManager = new LinearLayoutManager(this);
        mContentRv.setLayoutManager(mLayoutManager);
        mAdapter = new LongTextAdapter();
        mContentRv.setAdapter(mAdapter);

        mContentRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mTopHeight <= 0) {
                    return;
                }
                mScrollY += dy;
                if (mScrollY >= mTopHeight) {
                    updateStatusBar(true);
                    mTitleBar.setAlpha(1f);
                } else {
                    float alpha = 1f * mScrollY / mTopHeight;
                    if (alpha < 0.6f) {
                        updateStatusBar(false);
                    } else if (alpha > 0.9f) {
                        updateStatusBar(true);
                    }
                    mTitleBar.setAlpha(1f * mScrollY / mTopHeight);
                }
            }
        });
    }

    @Override
    public boolean isStatusBarDark() {
        return mIsDark;
    }

    private void updateStatusBar(boolean isDark) {
        if (mIsDark != isDark) {
            mIsDark = isDark;
            setStatusColor(this, mIsDark);
        }
    }


    private void initPresenter() {
        mPresenter = new LongTextPresenter(this);
        mPresenter.getFeedsInfo(mFeedId, mOwnerId);
    }

    @Override
    public void getFeedInfoSuccess(LongTextModel model) {
        List<BaseFeedItemModel> dataList = model.getDataList();
        calculateTopHeight(dataList);
        mAdapter.setDataList(model.getDataList());
    }

    private void calculateTopHeight(List<BaseFeedItemModel> dataList) {
        if (dataList != null) {
            if (dataList.size() > 0) {
                if (dataList.get(0).getUiType() == FeedItemUiType.UI_TYPE_COVER) {
                    mTopHeight += GlobalData.screenWidth * 3 / 4;

                    if (dataList.size() > 1) {
                        if (dataList.get(1).getUiType() == FeedItemUiType.UI_TYPE_OWNER) {
                            mTopHeight += DisplayUtils.dip2px(63.33f);
                        }
                    }

                    mTopHeight -= mTitleBar.getHeight();
                }
            }
        }
        if (mTopHeight <= 0) {
            mBgBar.setVisibility(View.GONE);
            updateStatusBar(true);
            mTitleBar.setAlpha(1f);
        }
    }

    @Override
    public void notifyFeedInfoDeleted() {
        ToastUtils.showToast(R.string.feeds_not_exist);
    }

    @Override
    public void notifyFeedInfoFailure() {
        ToastUtils.showToast(R.string.feeds_getinfo_failed);
    }

    @Override
    public <T> Observable.Transformer<T, T> bindUntilEvent() {
        return bindUntilEvent();
    }

    public static void open(Activity activity, String feedId, long ownerId) {
        Intent intent = new Intent(activity, LongTextActivity.class);
        intent.putExtra(EXTRA_FEED_ID, feedId);
        intent.putExtra(EXTRA_OWNER_ID, ownerId);
        activity.startActivity(intent);
    }
}
