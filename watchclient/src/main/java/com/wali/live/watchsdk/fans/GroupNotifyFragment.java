package com.wali.live.watchsdk.fans;

import android.content.DialogInterface;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.BaseActivity;
import com.base.dialog.MyAlertDialog;
import com.base.fragment.BaseEventBusFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.view.BackTitleBar;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.adapter.GroupNotifyAdapter;
import com.wali.live.watchsdk.fans.holder.GroupNotify.BaseNotifyHolder;
import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;
import com.wali.live.watchsdk.fans.presenter.GroupNotifyPresenter;
import com.wali.live.watchsdk.fans.push.event.GroupNotifyUpdateEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by zyh on 2017/11/22.
 *
 * @module 粉丝团页面
 */

public class GroupNotifyFragment extends BaseEventBusFragment implements View.OnClickListener, GroupNotifyPresenter.IView {
    private final static String TAG = "GroupNotifyFragment";

    private BackTitleBar mBackTitleBar;
    private RecyclerView mRecyclerView;
    private GroupNotifyAdapter mAdapter;
    private GroupNotifyPresenter mPresenter;
    private BaseNotifyHolder.OnItemClickListener mListener = new BaseNotifyHolder.OnItemClickListener() {
        @Override
        public void onAgreeJoin(GroupNotifyBaseModel model) {
            mPresenter.handleJoinGroup(model);
        }
    };

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_notifity_group, container, false);
    }

    @Override
    protected void bindView() {
        initView();
        initPresenter();
    }

    private void initView() {
        mBackTitleBar = $(R.id.back_title_bar);
        mBackTitleBar.setTitle(R.string.vfans_notify);
        mBackTitleBar.getRightTextBtn().setText(R.string.clear_text);
        $click(mBackTitleBar.getRightTextBtn(), this);
        $click(mBackTitleBar.getBackBtn(), this);

        mRecyclerView = $(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new GroupNotifyAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setListener(mListener);
    }

    private void initPresenter() {
        mPresenter = new GroupNotifyPresenter(this);
        mPresenter.loadDataFromDB();
        mPresenter.syncFansNotify();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.right_text_btn) {
            showClearNotifyDialog();
        } else if (i == R.id.back_iv) {
            finish();
        }
    }

    public void showClearNotifyDialog() {
        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(getContext());
        builder.setMessage(R.string.vfans_notify_clear_confirm);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPresenter.clearAllGroupNotify();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onDestroy() {
        mPresenter.markConversationAsRead();
        super.onDestroy();
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GroupNotifyUpdateEvent event) {
        mAdapter.setGroupNotifyBaseModels(event.allGroupNotifyList);
    }

    public static void openFragment(BaseActivity activity) {
        FragmentNaviUtils.addFragmentToBackStack(activity, R.id.main_act_container,
                GroupNotifyFragment.class, null, true, R.anim.slide_right_in, R.anim.slide_right_out);
    }

    @Override
    public void setGroupNotifyData(List<GroupNotifyBaseModel> models) {
        mAdapter.setGroupNotifyBaseModels(models);
    }
}
