package useroperate.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.viewpager.NestViewPager;
import com.common.view.viewpager.SlidingTabLayout;
import com.component.busilib.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import useroperate.OperateFriendActivity;
import useroperate.inter.IOperateStub;
import useroperate.view.OperateFriendView;

public class OperateFriendFragment extends BaseFragment {
    public final String TAG = "OperateFriendFragment";

    SlidingTabLayout mInviteTab;
    NestViewPager mInviteVp;
    ExImageView mIvBack;
    List<IOperateStub<UserInfoModel>> mIOperateStubList;

//    HashMap<Integer, String> mTitleList = new HashMap<>();
//    HashMap<Integer, OperateFriendView> mTitleAndViewMap = new HashMap<>();
    PagerAdapter mTabPagerAdapter;
    List<OperateFriendView>  mOperateFriendViews= new ArrayList<>();
    @Override
    public int initView() {
        return R.layout.operate_friend_fragment;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (mIOperateStubList == null || mIOperateStubList.size() == 0) {
            mIvBack.callOnClick();
            return;
        }

        mInviteTab = getRootView().findViewById(R.id.invite_tab);
        mInviteVp = getRootView().findViewById(R.id.invite_vp);
        mIvBack = getRootView().findViewById(R.id.iv_back);

        mInviteTab.setCustomTabView(R.layout.operate_tab_view, R.id.tab_tv);
        mInviteTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20));
        mInviteTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER);
        mInviteTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE);
        mInviteTab.setIndicatorWidth(U.getDisplayUtils().dip2px(67));
        mInviteTab.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(12));
        mInviteTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(28));
        mInviteTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(14));

        mOperateFriendViews.clear();
        for (IOperateStub<UserInfoModel> stub : mIOperateStubList) {
            switch (stub.getFriendType()) {
                case 0:
                    mOperateFriendViews.add(new OperateFriendView(this, UserInfoManager.RELATION.FRIENDS.getValue(), stub));
                    break;
                case 1:
                    mOperateFriendViews.add( new OperateFriendView(this, UserInfoManager.RELATION.FOLLOW.getValue(), stub));
                    break;
                case 2:
                    mOperateFriendViews.add( new OperateFriendView(this, UserInfoManager.RELATION.FANS.getValue(), stub));
                    break;
            }
        }

        if (mOperateFriendViews.size() > 1) {
            mInviteTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20));
        } else {
            mInviteTab.setSelectedIndicatorColors(U.getColor(R.color.transparent));
        }

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
                View view = mOperateFriendViews.get(position);
                if (container.indexOfChild(view) == -1) {
                    container.addView(view);
                }
                return view;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {

                return mOperateFriendViews.get(position).stub.getTitle();
            }

            @Override
            public int getCount() {
                return mOperateFriendViews.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == (object);
            }
        };

        mInviteVp.setAdapter(mTabPagerAdapter);
        mInviteTab.setViewPager(mInviteVp);
        mTabPagerAdapter.notifyDataSetChanged();

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() instanceof OperateFriendActivity) {
                    getActivity().finish();
                } else {
                    U.getFragmentUtils().popFragment(OperateFriendFragment.this);
                }
            }
        });
    }

    @Override
    public void setData(int type, @org.jetbrains.annotations.Nullable Object data) {
        if (type == 0) {
            mIOperateStubList = (List<IOperateStub<UserInfoModel>>) data;
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
