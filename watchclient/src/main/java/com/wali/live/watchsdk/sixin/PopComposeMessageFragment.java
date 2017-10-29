package com.wali.live.watchsdk.sixin;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.BaseActivity;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.log.MyLog;
import com.base.view.BackTitleBar;
import com.mi.live.data.user.User;
import com.wali.live.dao.SixinMessage;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.sixin.constant.SixinConstants;
import com.wali.live.watchsdk.sixin.message.SixinMessageModel;
import com.wali.live.watchsdk.sixin.presenter.ISixinMessageView;
import com.wali.live.watchsdk.sixin.presenter.SixinMessagePresenter;
import com.wali.live.watchsdk.sixin.recycler.SixinMessageAdapter;

import java.util.List;

import rx.Observable;

/**
 * Created by lan on 2017/10/27.
 */
public class PopComposeMessageFragment extends RxFragment implements View.OnClickListener, ISixinMessageView {
    private static final String EXTRA_USER = "extra_user";

    private View mBgView;

    private BackTitleBar mTitleBar;

    private SwipeRefreshLayout mRefreshLayout;

    private RecyclerView mMessageRv;
    private LinearLayoutManager mLayoutManager;
    private SixinMessageAdapter mMessageAdapter;

    private User mTarget;
    private SixinMessagePresenter mMessagePresenter;

    private boolean mIsScrollToLast;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected String getTAG() {
        return SixinConstants.LOG_PREFIX + getClass().getSimpleName();
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        initData(args);
    }

    private void initData(Bundle bundle) {
        if (bundle == null) {
            finish();
        }
        mTarget = (User) bundle.getSerializable(EXTRA_USER);
        if (mTarget == null) {
            finish();
        }
        MyLog.d(TAG, "user id=" + mTarget.getUid());
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_pop_compose_message, container, false);
    }

    @Override
    protected void bindView() {
        mBgView = $(R.id.bg_view);
        $click(mBgView, this);

        mTitleBar = $(R.id.title_bar);
        $click(mTitleBar.getBackBtn(), this);
        if (!TextUtils.isEmpty(mTarget.getNickname())) {
            mTitleBar.setTitle(mTarget.getNickname());
        } else {
            mTitleBar.setTitle(String.valueOf(mTarget.getUid()));
        }

        mRefreshLayout = $(R.id.swipe_refresh_layout);
        mRefreshLayout.setEnabled(false);

        mMessageRv = $(R.id.message_rv);

        mMessageAdapter = new SixinMessageAdapter();
        mMessageRv.setAdapter(mMessageAdapter);

        mMessageRv.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.ItemAnimator animator = mMessageRv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        mLayoutManager = new LinearLayoutManager(getContext());
        mMessageRv.setLayoutManager(mLayoutManager);
        mMessageRv.setHasFixedSize(true);

        initPresenter();
    }

    private void initPresenter() {
        mMessagePresenter = new SixinMessagePresenter(this);
        mMessagePresenter.firstLoadDataFromDB(mTarget.getUid(), SixinMessage.TARGET_TYPE_USER);
    }

    @Override
    public <T> Observable.Transformer<T, T> bindLifecycle() {
        return bindUntilEvent();
    }

    @Override
    public void loadDataSuccess(List<SixinMessageModel> messageModelList) {
        MyLog.d(TAG, "messageList size=" + messageModelList.size());
        if (messageModelList.size() > 0) {
            mMessageAdapter.setDataList(messageModelList);
            scrollToLastItem();
        }
    }

    private void scrollToLastItem() {
        mMessageRv.scrollToPosition(mMessageAdapter.getItemCount() - 1);
        mIsScrollToLast = true;
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() == null) {
            return false;
        }
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.bg_view || id == R.id.back_iv) {
            finish();
        }
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    public static void open(BaseActivity activity, User target, boolean isNeedSaveToStack) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_USER, target);
        if (isNeedSaveToStack) {
            FragmentNaviUtils.addFragmentToBackStack(activity, R.id.main_act_container, PopComposeMessageFragment.class, bundle, false, 0, 0);
        } else {
            FragmentNaviUtils.addFragmentAndResetArgument(activity, R.id.main_act_container, PopComposeMessageFragment.class, bundle, true, false, 0, 0);
        }
    }
}
