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
import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
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
import com.module.RouterConstants;
import com.module.msg.IMessageFragment;
import com.module.msg.follow.LastFollowModel;
import com.zq.dialog.InviteFriendDialog;
import com.zq.relation.fragment.SearchUserFragment;

import java.util.List;

import io.rong.imkit.R;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;

public class MessageFragment2 extends BaseFragment implements IMessageFragment, WeakRedDotManager.WeakRedDotListener {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    RelativeLayout mLatestFollowArea;
    ImageView mFollowAreaIcon;
    ExImageView mFollowRedDot;
    TextView mFollowTips;
    TextView mFollowTimeTv;
    RelativeLayout mContent;

    PopupWindow mPopupWindow;  // 弹窗
    RelativeLayout mSearchArea;
    RelativeLayout mInviteArea;

    InviteFriendDialog mInviteFriendDialog;

    Fragment mConversationListFragment; //获取融云的会话列表对象

    long mLastUpdateTime = 0;  //最新关注第一条刷新时间
    int mMessageFollowRedDotValue = 0;

    @Override
    public int initView() {
        return R.layout.conversation_list_fragment2;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mLatestFollowArea = (RelativeLayout) mRootView.findViewById(R.id.latest_follow_area);
        mFollowAreaIcon = (ImageView) mRootView.findViewById(R.id.follow_area_icon);
        mFollowRedDot = (ExImageView) mRootView.findViewById(R.id.follow_red_dot);
        mFollowTips = (TextView) mRootView.findViewById(R.id.follow_tips);
        mFollowTimeTv = (TextView) mRootView.findViewById(R.id.follow_time_tv);
        mContent = (RelativeLayout) mRootView.findViewById(R.id.content);

        mConversationListFragment = initConversationList();
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, mConversationListFragment);
        transaction.commit();

        LinearLayout linearLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(com.component.busilib.R.layout.add_friend_pop_window_layout, null);
        mSearchArea = (RelativeLayout) linearLayout.findViewById(com.component.busilib.R.id.search_area);
        mInviteArea = (RelativeLayout) linearLayout.findViewById(com.component.busilib.R.id.invite_area);
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

        mLatestFollowArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                StatisticsAdapter.recordCountEvent("IMtab", "newfollow", null);
                ARouter.getInstance().build(RouterConstants.ACTIVITY_LAST_FOLLOW)
                        .navigation();

                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.MESSAGE_FOLLOW_RED_ROD_TYPE, 0);
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
        getLastRelationOne(false);
    }

    private void getLastRelationOne(boolean flag) {
        long now = System.currentTimeMillis();
        if (!flag) {
            if ((now - mLastUpdateTime) < 60 * 1000) {
                return;
            }
        }

        UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(userInfoServerApi.getLatestRelation(1), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<LastFollowModel> list = JSON.parseArray(result.getData().getString("users"), LastFollowModel.class);
                    showLastRelation(list);
                }
            }
        }, this);
    }

    private void showLastRelation(List<LastFollowModel> list) {
        if (list != null && list.size() != 0) {
            LastFollowModel lastFollowModel = list.get(0);
            if (lastFollowModel != null) {
                mFollowTimeTv.setText(U.getDateTimeUtils().getDateTimeString(lastFollowModel.getTimeMs(), false, getContext()));
                if (lastFollowModel.getRelation() == LastFollowModel.RELATION_FOLLOW_ME) {
                    mFollowTips.setVisibility(View.VISIBLE);
                    mFollowTimeTv.setVisibility(View.VISIBLE);
                    mFollowTips.setText(lastFollowModel.getNickname() + "关注了你");
                } else if (lastFollowModel.getRelation() == LastFollowModel.RELATION_ME_FOLLOW) {
                    mFollowTips.setVisibility(View.VISIBLE);
                    mFollowTimeTv.setVisibility(View.VISIBLE);
                    mFollowTips.setText("你关注了" + lastFollowModel.getNickname());
                } else {
                    mFollowTips.setVisibility(View.GONE);
                    mFollowTimeTv.setVisibility(View.GONE);
                }
            }
        } else {
            // TODO: 2019/4/24  暂无记录
        }
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
    protected void onFragmentInvisible() {
        super.onFragmentInvisible();
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
            ConversationListFragment listFragment = new ConversationListFragment();
            Uri uri = Uri.parse("io.rong://" + U.getAppInfoUtils().getPackageName()).buildUpon()
                    .appendPath("conversation_list_activity")
                    .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                    .build();
            listFragment.setUri(uri);
            return listFragment;
        } else {
            return mConversationListFragment;
        }
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
                WeakRedDotManager.MESSAGE_FOLLOW_RED_ROD_TYPE};
    }

    @Override
    public void onWeakRedDotChange(int type, int value) {
        if (type == WeakRedDotManager.MESSAGE_FOLLOW_RED_ROD_TYPE) {
            mMessageFollowRedDotValue = value;
        }

        refreshMessageRedDot();
    }

    private void refreshMessageRedDot() {
        if (mMessageFollowRedDotValue < 1) {
            mFollowRedDot.setVisibility(View.GONE);
        } else {
            mFollowRedDot.setVisibility(View.VISIBLE);
        }
    }
}
