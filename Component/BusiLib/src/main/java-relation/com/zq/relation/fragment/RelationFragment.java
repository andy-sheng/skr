package com.zq.relation.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.common.view.viewpager.NestViewPager;
import com.common.view.viewpager.SlidingTabLayout;
import com.component.busilib.R;
import com.jakewharton.rxbinding2.view.RxView;
import com.zq.relation.view.RelationView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import model.RelationNumModel;

/**
 * 关系列表
 */
public class RelationFragment extends BaseFragment {

    public static final int FROM_FRIENDS = 0;
    public static final int FROM_FANS = 1;
    public static final int FROM_FOLLOW = 2;

    public static final String FROM_PAGE_KEY = "from_page_key";
    public static final String FRIEND_NUM_KEY = "friend_num_key";
    public static final String FANS_NUM_KEY = "fans_num_key";
    public static final String FOLLOW_NUM_KEY = "follow_num_key";

    CommonTitleBar mTitlebar;
    LinearLayout mContainer;
    SlidingTabLayout mRelationTab;
    View mSplitLine;
    NestViewPager mRelationVp;

    TextView mFriend;
    TextView mFans;
    TextView mFollow;

    PagerAdapter mTabPagerAdapter;

    int mFriendNum = 0;  // 好友数
    int mFansNum = 0;    // 粉丝数
    int mFocusNum = 0;   // 关注数

    HashMap<Integer, RelationView> mTitleAndViewMap = new HashMap<>();

    @Override
    public int initView() {
        return R.layout.relation_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mContainer = (LinearLayout) mRootView.findViewById(R.id.container);
        mRelationTab = (SlidingTabLayout) mRootView.findViewById(R.id.relation_tab);
        mSplitLine = (View) mRootView.findViewById(R.id.split_line);
        mRelationVp = (NestViewPager) mRootView.findViewById(R.id.relation_vp);

        mFriend = (TextView) mRootView.findViewById(R.id.friend);
        mFans = (TextView) mRootView.findViewById(R.id.fans);
        mFollow = (TextView) mRootView.findViewById(R.id.follow);


        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                        U.getFragmentUtils().popFragment(RelationFragment.this);
                    }
                });

        mTitleAndViewMap.put(0, new RelationView(getContext(), UserInfoManager.RELATION_FRIENDS));
        mTitleAndViewMap.put(1, new RelationView(getContext(), UserInfoManager.RELATION_FANS));
        mTitleAndViewMap.put(2, new RelationView(getContext(), UserInfoManager.RELATION_FOLLOW));

        mRelationTab.setCustomTabView(R.layout.relation_tab_view, R.id.tab_tv);
        mRelationTab.setSelectedIndicatorColors(Color.parseColor("#FE8400"));
        mRelationTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER);
        mRelationTab.setIndicatorWidth(U.getDisplayUtils().dip2px(27));
        mRelationTab.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(5));
        mRelationTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(4));
        mRelationTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(2));


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

            @Override
            public int getItemPosition(@NonNull Object object) {
                return POSITION_NONE;
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

        mRelationVp.setAdapter(mTabPagerAdapter);
        mRelationTab.setViewPager(mRelationVp);
        mTabPagerAdapter.notifyDataSetChanged();

        Bundle bundle = getArguments();
        if (bundle != null) {
            int from = bundle.getInt(FROM_PAGE_KEY);
            mFriendNum = bundle.getInt(FRIEND_NUM_KEY);
            mFansNum = bundle.getInt(FANS_NUM_KEY);
            mFocusNum = bundle.getInt(FOLLOW_NUM_KEY);
            mRelationVp.setCurrentItem(from);
            refreshRelationNums();
        } else {
            getRelationNums();
        }

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);
    }

    private void getRelationNums() {
        UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(userInfoServerApi.getRelationNum((int) MyUserInfoManager.getInstance().getUid()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<RelationNumModel> relationNumModels = JSON.parseArray(result.getData().getString("cnt"), RelationNumModel.class);
                    if (relationNumModels != null && relationNumModels.size() > 0) {
                        for (RelationNumModel mode : relationNumModels) {
                            if (mode.getRelation() == UserInfoManager.RELATION_FRIENDS) {
                                mFriendNum = mode.getCnt();
                            } else if (mode.getRelation() == UserInfoManager.RELATION_FANS) {
                                mFansNum = mode.getCnt();
                            } else if (mode.getRelation() == UserInfoManager.RELATION_FOLLOW) {
                                mFocusNum = mode.getCnt();
                            }
                        }
                    }
                    refreshRelationNums();
                }
            }
        });

    }

    public void refreshRelationNums() {
        mFriend.setText(String.format(getString(R.string.friends_num), mFriendNum));
        mFollow.setText(String.format(getString(R.string.follows_num), mFocusNum));
        mFans.setText(String.format(getString(R.string.fans_num), mFansNum));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        if (event.type == RelationChangeEvent.FOLLOW_TYPE) {
            if (event.isFriend) {
                // 新增好友,好友数加1
                mFriendNum = mFriendNum + 1;
                mFocusNum = mFocusNum + 1;
            } else if (event.isFollow) {
                // 新增关注,关注数加1
                mFocusNum = mFocusNum + 1;
            }
        } else if (event.type == RelationChangeEvent.UNFOLLOW_TYPE) {
            // 关注数减1
            mFocusNum = mFocusNum - 1;
            // TODO: 2019/1/17 怎么判断之前也是好友
            if (event.isOldFriend) {
                mFriendNum = mFriendNum - 1;
            }
        }

        refreshRelationNums();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mTitleAndViewMap != null) {
            for (RelationView view : mTitleAndViewMap.values()) {
                view.destroy();
            }
        }

        U.getSoundUtils().release(TAG);
    }
}
