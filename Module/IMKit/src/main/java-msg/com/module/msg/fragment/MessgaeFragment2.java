package com.module.msg.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.clipboard.ClipboardUtils;
import com.common.core.kouling.SkrKouLingUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.module.RouterConstants;
import com.module.common.ICallback;
import com.module.msg.IMessageFragment;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.relation.fragment.SearchFriendFragment;

import io.rong.imkit.R;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;

public class MessgaeFragment2 extends BaseFragment implements IMessageFragment {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    RelativeLayout mLatestFollowArea;
    RelativeLayout mContent;

    PopupWindow mPopupWindow;  // 弹窗
    RelativeLayout mSearchArea;
    RelativeLayout mInviteArea;

    DialogPlus mShareDialog;
    TextView mTvWeixinShare;
    TextView mTvQqShare;

    Fragment mConversationListFragment; //获取融云的会话列表对象

    @Override
    public int initView() {
        return R.layout.conversation_list_fragment2;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mMainActContainer = (RelativeLayout)mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar)mRootView.findViewById(R.id.titlebar);
        mLatestFollowArea = (RelativeLayout)mRootView.findViewById(R.id.latest_follow_area);
        mContent = (RelativeLayout)mRootView.findViewById(R.id.content);

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
                        FragmentUtils.newAddParamsBuilder(getActivity(), SearchFriendFragment.class)
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

        mTitlebar.getRightImageButton().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                }
                mPopupWindow.setWidth(U.getDisplayUtils().dip2px(118));
                mPopupWindow.setHeight(U.getDisplayUtils().dip2px(115));
                mPopupWindow.showAsDropDown(mTitlebar.getRightImageButton(), -U.getDisplayUtils().dip2px(80), -U.getDisplayUtils().dip2px(5));
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
    }

    private void showShareDialog() {
        if (mShareDialog == null) {
            mShareDialog = DialogPlus.newDialog(getContext())
                    .setContentHolder(new ViewHolder(com.component.busilib.R.layout.invite_friend_panel))
                    .setContentBackgroundResource(com.component.busilib.R.color.transparent)
                    .setOverlayBackgroundResource(com.component.busilib.R.color.black_trans_50)
                    .setExpanded(false)
                    .setGravity(Gravity.BOTTOM)
                    .create();

            mTvWeixinShare = (TextView) mShareDialog.findViewById(com.component.busilib.R.id.tv_weixin_share);
            mTvQqShare = (TextView) mShareDialog.findViewById(com.component.busilib.R.id.tv_qq_share);
            mTvWeixinShare.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    SkrKouLingUtils.genReqFollowKouling((int) MyUserInfoManager.getInstance().getUid(), MyUserInfoManager.getInstance().getNickName(), new ICallback() {
                        @Override
                        public void onSucess(Object obj) {
                            mShareDialog.dismiss();
                            ClipboardUtils.setCopy((String) obj);
                            Intent intent = U.getActivityUtils().getLaunchIntentForPackage("com.tencent.mm");
                            if (intent!=null && null != intent.resolveActivity(U.app().getPackageManager())) {
                                startActivity(intent);
                                U.getToastUtil().showLong("请将口令粘贴给你的好友");
                            }else{
                                U.getToastUtil().showLong("未安装微信,请将口令粘贴给你的好友");
                            }
                        }

                        @Override
                        public void onFailed(Object obj, int errcode, String message) {
                            U.getToastUtil().showShort("口令生成失败");
                        }
                    });
                }
            });

            mTvQqShare.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    // TODO: 2019/3/24 邀请好友
                    SkrKouLingUtils.genReqFollowKouling((int) MyUserInfoManager.getInstance().getUid(), MyUserInfoManager.getInstance().getNickName(), new ICallback() {
                        @Override
                        public void onSucess(Object obj) {
                            mShareDialog.dismiss();
                            ClipboardUtils.setCopy((String) obj);
                            Intent intent = U.getActivityUtils().getLaunchIntentForPackage("com.tencent.mobileqq");
                            if (intent!=null && null != intent.resolveActivity(U.app().getPackageManager())) {
                                startActivity(intent);
                                U.getToastUtil().showLong("请将口令粘贴给你的好友");
                            }else{
                                U.getToastUtil().showLong("未安装QQ,请将口令粘贴给你的好友");
                            }
                        }

                        @Override
                        public void onFailed(Object obj, int errcode, String message) {
                            U.getToastUtil().showShort("口令生成失败");
                        }
                    });
                }
            });
        }

        if (!mShareDialog.isShowing()) {
            mShareDialog.show();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }

        if (mShareDialog != null) {
            mShareDialog.dismiss();
        }
    }

    @Override
    protected void onFragmentInvisible() {
        super.onFragmentInvisible();
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }

        if (mShareDialog != null) {
            mShareDialog.dismiss();
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
}
