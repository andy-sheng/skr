package com.module.msg.fragment;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.notification.NotificationManager;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.common.view.viewpager.NestViewPager;
import com.common.view.viewpager.SlidingTabLayout;
import com.component.busilib.manager.WeakRedDotManager;
import com.module.msg.IMessageFragment;
import com.module.msg.friend.FriendFragment;
import com.zq.relation.fragment.RelationFragment;

import java.util.HashMap;

import io.rong.imkit.R;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;

public class MessageFragment extends BaseFragment implements IMessageFragment, WeakRedDotManager.WeakRedDotListener {

    public final static String TAG = "MessageFragment";

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    LinearLayout mContainer;
    SlidingTabLayout mMessageTab;
    View mSplitLine;
    NestViewPager mMessageVp;

    Fragment mConversationListFragment; //获取融云的会话列表对象

    HashMap<Integer, String> mTitleList = new HashMap<>();

    int mFansRedDotValue = 0;
    int mFriendRedDotValue = 0;

    @Override
    public int initView() {
        return R.layout.conversation_list_fragment;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mContainer = (LinearLayout) mRootView.findViewById(R.id.container);
        mMessageTab = (SlidingTabLayout) mRootView.findViewById(R.id.message_tab);
        mSplitLine = (View) mRootView.findViewById(R.id.split_line);
        mMessageVp = (NestViewPager) mRootView.findViewById(R.id.message_vp);

        mTitlebar.getRightImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTitlebar.getRightImageButton().setImageResource(R.drawable.friend_book_icon);
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.FANS_RED_ROD_TYPE, 1);
                WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.FRIEND_RED_ROD_TYPE, 1);

                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), RelationFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });

        mTitleList.put(0, "消息");
        mTitleList.put(1, "好友");

        mMessageTab.setCustomTabView(R.layout.relation_tab_view, R.id.tab_tv);
        mMessageTab.setSelectedIndicatorColors(Color.parseColor("#FE8400"));
        mMessageTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER);
        mMessageTab.setIndicatorWidth(U.getDisplayUtils().dip2px(27));
        mMessageTab.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(5));
        mMessageTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(4));
        mMessageTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(2));

        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getActivity().getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                MyLog.d(TAG, "getItem" + " position=" + position);
                if (position == 0) {
                    return initConversationList();
                } else if (position == 1) {
                    return new FriendFragment();
                }
                return null;
            }

            @Override
            public int getCount() {
                return mTitleList.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return mTitleList.get(position);
            }
        };

        mMessageVp.setAdapter(fragmentPagerAdapter);
        mMessageTab.setViewPager(mMessageVp);

        WeakRedDotManager.getInstance().addListener(this);

        mFansRedDotValue = U.getPreferenceUtils().getSettingInt(WeakRedDotManager.SP_KEY_NEW_FANS, 0);
        mFriendRedDotValue = U.getPreferenceUtils().getSettingInt(WeakRedDotManager.SP_KEY_NEW_FRIEND, 0);
        refreshMessageRedDot();
    }

    @Override
    public void destroy() {
        super.destroy();
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
    public boolean isInViewPager() {
        return true;
    }

    @Override
    public int[] acceptType() {
        return new int[]{
                WeakRedDotManager.FRIEND_RED_ROD_TYPE,
                WeakRedDotManager.FANS_RED_ROD_TYPE};
    }

    @Override
    public void onWeakRedDotChange(int type, int value) {
        if (type == WeakRedDotManager.FANS_RED_ROD_TYPE) {
            mFansRedDotValue = value;
        } else if (type == WeakRedDotManager.FRIEND_RED_ROD_TYPE) {
            mFriendRedDotValue = value;
        }

        refreshMessageRedDot();
    }

    private void refreshMessageRedDot() {
        if (mFansRedDotValue < 2 && mFriendRedDotValue < 2) {
            mTitlebar.getRightImageButton().setImageResource(R.drawable.friend_book_icon);
        } else {
            mTitlebar.getRightImageButton().setImageResource(R.drawable.friend_book_red_icon);
        }
    }
}
