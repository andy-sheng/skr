package com.wali.live.watchsdk.videodetail.view;

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.user.User;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.watchsdk.R;

/**
 * Created by yangli on 2017/06/01.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 详情信息视图
 */
public class InfoAreaView implements View.OnClickListener,
        IComponentView<InfoAreaView.IPresenter, InfoAreaView.IView> {
    private static final String TAG = "InfoAreaView";

    @Nullable
    protected IPresenter mPresenter;

    private View mContentView;
    private TextView mFollowTv;
    private TextView mFansCntTv;
    private TextView mWatchNumTv;
    private TextView mTimestampTv;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) mContentView.findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View v) {
        if (mPresenter == null) {
            return;
        }
        int i = v.getId();
        if (i == R.id.focus) {
            mPresenter.followUser();
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
        if (mPresenter != null) {
            mPresenter.syncFeedsInfo();
            mPresenter.syncUserInfo();
        }
    }

    public InfoAreaView(View contentView) {
        mContentView = contentView;
        mFansCntTv = $(R.id.fans_count_tv);
        mWatchNumTv = $(R.id.watch_num_tv);
        mTimestampTv = $(R.id.timestamp_tv);
        mFollowTv = $(R.id.focus);
        $click(mFollowTv, this);
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) mContentView;
            }

            @Override
            public void onUserInfo(User user) {
                if (user == null) {
                    return;
                }
                if (user.getUid() != UserAccountManager.getInstance().getUuidAsLong()) {
                    mFollowTv.setVisibility(View.VISIBLE);
                    if (user.isBothwayFollowing()) {
                        mFollowTv.setEnabled(false);
                        mFollowTv.setText(R.string.follow_both);
                    } else if (user.isFocused()) {
                        mFollowTv.setEnabled(false);
                        mFollowTv.setText(R.string.already_followed);
                    } else {
                        mFollowTv.setEnabled(true);
                        mFollowTv.setText(R.string.follow);
                    }
                }
                int fansCnt = user.getFansNum();
                mFansCntTv.setText(mContentView.getResources().getQuantityString(
                        R.plurals.feeds_fans_count_formatter, fansCnt, fansCnt));
            }

            @Override
            public void onFeedsInfo(long timestamp, int viewerCnt) {
                mTimestampTv.setText(DateFormat.format("yyyy-MM-dd HH:mm", timestamp).toString());
                mWatchNumTv.setText(mContentView.getResources().getQuantityString(
                        R.plurals.live_end_viewer_cnt, viewerCnt, viewerCnt));
            }

            @Override
            public void onFollowed(boolean isBothWay) {
                mFollowTv.setEnabled(false);
                mFollowTv.setText(isBothWay ? R.string.follow_both : R.string.already_followed);
            }

            @Override
            public void onUnFollowed() {
                mFollowTv.setEnabled(true);
                mFollowTv.setText(R.string.follow);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 拉取用户信息
         */
        void syncUserInfo();

        /**
         * 拉取Feeds信息
         */
        void syncFeedsInfo();

        /**
         * 关注
         */
        void followUser();
    }

    public interface IView extends IViewProxy {
        /**
         * 拉取到用户信息
         */
        void onUserInfo(User user);

        /**
         * 拉取Feeds信息
         */
        void onFeedsInfo(long timestamp, int viewerCnt);

        /**
         * 关注成功
         */
        void onFollowed(boolean isBothWay);

        /**
         * 重置关注按钮状态
         */
        void onUnFollowed();

    }
}
