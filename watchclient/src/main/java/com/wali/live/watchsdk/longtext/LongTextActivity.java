package com.wali.live.watchsdk.longtext;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.longtext.adapter.LongTextAdapter;
import com.wali.live.watchsdk.longtext.model.LongTextModel;
import com.wali.live.watchsdk.longtext.presenter.ILongTextView;
import com.wali.live.watchsdk.longtext.presenter.LongTextPresenter;

import rx.Observable;

/**
 * Created by lan on 2017/9/19.
 */
public class LongTextActivity extends BaseSdkActivity implements ILongTextView {
    private static final String EXTRA_FEED_ID = "extra_feed_id";
    private static final String EXTRA_OWNER_ID = "extra_owner_id";

    private BackTitleBar mTitleBar;

    private RecyclerView mContentRv;
    private LinearLayoutManager mLayoutManager;
    private LongTextAdapter mAdapter;

    private LongTextPresenter mPresenter;

    private String mFeedId;
    private long mOwnerId;

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
        mTitleBar = $(R.id.title_bar);
        mContentRv = $(R.id.content_rv);

        mLayoutManager = new LinearLayoutManager(this);
        mContentRv.setLayoutManager(mLayoutManager);
        mAdapter = new LongTextAdapter();
        mContentRv.setAdapter(mAdapter);
    }

    private void initPresenter() {
        mPresenter = new LongTextPresenter(this);
        mPresenter.getFeedsInfo(mFeedId, mOwnerId);
    }

    @Override
    public void getFeedInfoSuccess(LongTextModel model) {
        mAdapter.setDataList(model.getDataList());
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
    public <T> Observable.Transformer<T, T> bindLifecycle() {
        return bindUntilEvent();
    }

    public static void open(Activity activity, String feedId, long ownerId) {
        Intent intent = new Intent(activity, LongTextActivity.class);
        intent.putExtra(EXTRA_FEED_ID, feedId);
        intent.putExtra(EXTRA_OWNER_ID, ownerId);
        activity.startActivity(intent);
    }
}
