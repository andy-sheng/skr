package com.module.msg.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.manager.WeakRedDotManager;
import com.component.dialog.InviteFriendDialog;
import com.component.relation.fragment.SearchUserFragment;
import com.module.RouterConstants;
import com.module.msg.IMessageFragment;
import com.module.msg.follow.LastFollowModel;

import java.util.List;

import io.rong.imkit.R;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;

public class MessageFragment2 extends BaseFragment implements IMessageFragment, WeakRedDotManager.WeakRedDotListener {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    RelativeLayout mContent;

    PopupWindow mPopupWindow;  // 弹窗
    RelativeLayout mSearchArea;
    RelativeLayout mInviteArea;

    InviteFriendDialog mInviteFriendDialog;

    MyConversationListFragment mConversationListFragment; //获取融云的会话列表对象

    long mLastUpdateTime = 0;  //最新关注第一条刷新时间
    int mMessageFollowRedDotValue = 0;

    @Override
    public int initView() {
        return R.layout.conversation_list_fragment2;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = getRootView().findViewById(R.id.main_act_container);
        mTitlebar = getRootView().findViewById(R.id.titlebar);
        mContent = getRootView().findViewById(R.id.content);

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, initConversationList());
        transaction.commit();

        LinearLayout linearLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(com.component.busilib.R.layout.add_friend_pop_window_layout, null);
        mSearchArea = linearLayout.findViewById(com.component.busilib.R.id.search_area);
        mInviteArea = linearLayout.findViewById(com.component.busilib.R.id.invite_area);
        mPopupWindow = new PopupWindow(linearLayout);
        mPopupWindow.setOutsideTouchable(true);

        mSearchArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                }
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), SearchUserFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });

        mInviteArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                }
                showShareDialog();
            }
        });

        mTitlebar.getRightTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                }
                mPopupWindow.setWidth(U.getDisplayUtils().dip2px(118));
                mPopupWindow.setHeight(U.getDisplayUtils().dip2px(115));
                mPopupWindow.showAsDropDown(mTitlebar.getRightTextView(), -U.getDisplayUtils().dip2px(80), -U.getDisplayUtils().dip2px(5));
            }
        });

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_RELATION)
                        .navigation();
            }
        });

        WeakRedDotManager.getInstance().addListener(this);
        mMessageFollowRedDotValue = U.getPreferenceUtils().getSettingInt(WeakRedDotManager.SP_KEY_NEW_MESSAGE_FOLLOW, 0);
        refreshMessageRedDot();
    }

    private void showShareDialog() {
        if (mInviteFriendDialog == null) {
            mInviteFriendDialog = new InviteFriendDialog(getContext(), InviteFriendDialog.INVITE_GRAB_FRIEND, 0, 0, 0, null);
        }
        mInviteFriendDialog.show();
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        StatisticsAdapter.recordCountEvent("IMtab", "expose", null);
        getLastNews(false);
    }

    private void getLastNews(boolean flag) {
        long now = System.currentTimeMillis();
        if (!flag) {
            if ((now - mLastUpdateTime) < 60 * 1000) {
                return;
            }
        }

        UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(userInfoServerApi.getLatestNews(MyUserInfoManager.INSTANCE.getUid()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    String str = result.getData().getString("latestNews");
                    long timeMs = result.getData().getLongValue("timeMs");
                }
            }
        }, this);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }

        if (mInviteFriendDialog != null) {
            mInviteFriendDialog.dismiss(false);
        }

        WeakRedDotManager.getInstance().removeListener(this);
    }

    @Override
    protected void onFragmentInvisible(int from) {
        super.onFragmentInvisible(from);
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }

        if (mInviteFriendDialog != null) {
            mInviteFriendDialog.dismiss(false);
        }
    }

    // 会话列表的Fragment
    private Fragment initConversationList() {
        if (mConversationListFragment == null) {
            mConversationListFragment = new MyConversationListFragment();
            Uri uri = Uri.parse("io.rong://" + U.getAppInfoUtils().getPackageName()).buildUpon()
                    .appendPath("conversation_list_activity")
                    .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                    .build();
            mConversationListFragment.setUri(uri);
        }
        return mConversationListFragment;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean isInViewPager() {
        return true;
    }

    @Override
    public int[] acceptType() {
        return new int[]{
                WeakRedDotManager.MESSAGE_FOLLOW_RED_ROD_TYPE
                , WeakRedDotManager.MESSAGE_POSTS_LIKE_TYPE
                , WeakRedDotManager.MESSAGE_POSTS_COMMENT_LIKE_TYPE
                , WeakRedDotManager.MESSAGE_POSTS_COMMENT_ADD_TYPE};
    }

    @Override
    public void onWeakRedDotChange(int type, int value) {
        if (type == WeakRedDotManager.MESSAGE_FOLLOW_RED_ROD_TYPE
                || type == WeakRedDotManager.MESSAGE_POSTS_LIKE_TYPE
                || type == WeakRedDotManager.MESSAGE_POSTS_COMMENT_LIKE_TYPE
                || type == WeakRedDotManager.MESSAGE_POSTS_COMMENT_ADD_TYPE) {
            mMessageFollowRedDotValue = value;
        }

        refreshMessageRedDot();
        getLastNews(true);
    }

    private void refreshMessageRedDot() {

    }
}
