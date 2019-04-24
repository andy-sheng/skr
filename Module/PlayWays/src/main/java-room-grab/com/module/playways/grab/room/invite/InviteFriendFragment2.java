package com.module.playways.grab.room.invite;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.viewpager.NestViewPager;
import com.common.view.viewpager.SlidingTabLayout;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;

import java.util.HashMap;

public class InviteFriendFragment2 extends BaseFragment {

    SlidingTabLayout mMessageTab;
    NestViewPager mMessageVp;
    ExImageView mIvBack;

    GrabRoomData mRoomData;

    HashMap<Integer, String> mTitleList = new HashMap<>();
    HashMap<Integer, InviteFriendView> mTitleAndViewMap = new HashMap<>();
    PagerAdapter mTabPagerAdapter;

    @Override
    public int initView() {
        return R.layout.invite_friend_fragment2;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMessageTab = (SlidingTabLayout) mRootView.findViewById(R.id.message_tab);
        mMessageVp = (NestViewPager) mRootView.findViewById(R.id.message_vp);
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);

        mMessageTab.setCustomTabView(R.layout.relation_tab_view, R.id.tab_tv);
        mMessageTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20));
        mMessageTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER);
        mMessageTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE);
        mMessageTab.setIndicatorWidth(U.getDisplayUtils().dip2px(67));
        mMessageTab.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(12));
        mMessageTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(28));
        mMessageTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(14));

        mTitleList.put(0, "好友");
        mTitleList.put(1, "粉丝");

        mTitleAndViewMap.put(0, new InviteFriendView(this, mRoomData.getGameId(), UserInfoManager.RELATION_FRIENDS));
        mTitleAndViewMap.put(1, new InviteFriendView(this, mRoomData.getGameId(), UserInfoManager.RELATION_FANS));

        mTabPagerAdapter = new PagerAdapter() {

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                MyLog.d(TAG, "destroyItem" + " container=" + container + " position=" + position + " object=" + object);
                container.removeView((View) object);
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                MyLog.d(TAG, "instantiateItem" + " container=" + container + " position=" + position);
                View view = mTitleAndViewMap.get(position);
                if (container.indexOfChild(view) == -1) {
                    container.addView(view);
                }
                return view;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return mTitleList.get(position);
            }

            @Override
            public int getCount() {
                return mTitleAndViewMap.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == (object);
            }
        };

        mMessageVp.setAdapter(mTabPagerAdapter);
        mMessageTab.setViewPager(mMessageVp);
        mTabPagerAdapter.notifyDataSetChanged();

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(InviteFriendFragment2.this);
            }
        });
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (GrabRoomData) data;
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
