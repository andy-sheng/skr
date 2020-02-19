package com.module.playways.grab.room.invite.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.share.SharePlatform;
import com.common.core.userinfo.UserInfoManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.common.view.viewpager.NestViewPager;
import com.common.view.viewpager.SlidingTabLayout;
import com.component.busilib.constans.GameModeType;
import com.component.dialog.InviteFriendDialog;
import com.module.playways.R;
import com.module.playways.grab.room.invite.IInviteCallBack;
import com.module.playways.grab.room.invite.InviteFriendActivity;
import com.module.playways.grab.room.invite.model.ShareModel;
import com.module.playways.grab.room.invite.view.InviteFriendView;
import com.module.playways.grab.room.invite.view.InviteShareFriendView;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public class InviteFriendFragment2 extends BaseFragment {
    public final String TAG = "InviteFriendFragment2";

    SlidingTabLayout mInviteTab;
    NestViewPager mInviteVp;
    ExImageView mIvBack;
    InviteShareFriendView mShareView;

    HashMap<Integer, String> mTitleList = new HashMap<>();
    HashMap<Integer, InviteFriendView> mTitleAndViewMap = new HashMap<>();
    PagerAdapter mTabPagerAdapter;

    InviteFriendDialog mInviteFriendDialog;

    String mKouLingToken = "";
    int mFrom;

    IInviteCallBack mInviteCallBack;

    @Override
    public int initView() {
        return R.layout.invite_friend_fragment2;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (mInviteCallBack == null) {
            if (getActivity() instanceof InviteFriendActivity) {
                getActivity().finish();
            } else {
                finish();
            }

            return;
        }

        mFrom = mInviteCallBack.getFrom();
        mInviteTab = getRootView().findViewById(R.id.invite_tab);
        mInviteVp = getRootView().findViewById(R.id.invite_vp);
        mIvBack = getRootView().findViewById(R.id.iv_back);
        mShareView = getRootView().findViewById(R.id.share_view);

        mInviteTab.setCustomTabView(R.layout.relation_tab_view, R.id.tab_tv);
        mInviteTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER);
        mInviteTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE);
        mInviteTab.setIndicatorWidth(U.getDisplayUtils().dip2px(67));
        mInviteTab.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(12));
        mInviteTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(28));
        mInviteTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(14));

        if (mFrom == GameModeType.GAME_MODE_GRAB || mFrom == GameModeType.GAME_MODE_MIC || mFrom == GameModeType.GAME_MODE_PARTY) {
            mInviteTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20));
            mTitleList.put(0, "好友");
            mTitleList.put(1, "粉丝");
            mTitleAndViewMap.put(0, new InviteFriendView(this, mFrom, UserInfoManager.RELATION.FRIENDS.getValue(), mInviteCallBack));
            mTitleAndViewMap.put(1, new InviteFriendView(this, mFrom, UserInfoManager.RELATION.FANS.getValue(), mInviteCallBack));
        } else if (mFrom == GameModeType.GAME_MODE_DOUBLE || mFrom == GameModeType.GAME_MODE_RELAY) {
            mInviteTab.setSelectedIndicatorColors(U.getColor(R.color.transparent));
            mTitleList.put(0, "好友");
            mTitleAndViewMap.put(0, new InviteFriendView(this, mFrom, UserInfoManager.RELATION.FRIENDS.getValue(), mInviteCallBack));
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

        mInviteVp.setAdapter(mTabPagerAdapter);
        mInviteTab.setViewPager(mInviteVp);
        mTabPagerAdapter.notifyDataSetChanged();

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() instanceof InviteFriendActivity) {
                    getActivity().finish();
                } else {
                    U.getFragmentUtils().popFragment(InviteFriendFragment2.this);
                }
            }
        });

        mShareView.setData(mFrom);
        mShareView.setListener(new RecyclerOnItemClickListener<ShareModel>() {
            @Override
            public void onItemClicked(View view, int position, ShareModel model) {
                int shareType = model.getShareType();
                switch (shareType) {
                    case ShareModel.SHARE_TYPE_CIPHER:
                        showShareDialog();
                        break;
                    case ShareModel.SHARE_TYPE_QQ: {
                        Intent intent = U.getActivityUtils().getLaunchIntentForPackage("com.tencent.mobileqq");
                        if (intent != null && null != intent.resolveActivity(U.app().getPackageManager())) {
                            shareUrl(SharePlatform.QQ);
                        } else {
                            U.getToastUtil().showShort("未安装QQ");
                        }
                    }
                    break;
                    case ShareModel.SHARE_TYPE_QQ_QZON: {
                        Intent intent = U.getActivityUtils().getLaunchIntentForPackage("com.tencent.mobileqq");
                        if (intent != null && null != intent.resolveActivity(U.app().getPackageManager())) {
                            shareUrl(SharePlatform.QZONE);
                        } else {
                            U.getToastUtil().showShort("未安装QQ");
                        }
                    }
                    break;
                    case ShareModel.SHARE_TYPE_WECHAT: {
                        Intent intent = U.getActivityUtils().getLaunchIntentForPackage("com.tencent.mm");
                        if (intent != null && null != intent.resolveActivity(U.app().getPackageManager())) {
                            shareUrl(SharePlatform.WEIXIN);
                        } else {
                            U.getToastUtil().showShort("未安装微信");
                        }
                    }
                    break;
                    case ShareModel.SHARE_TYPE_WECHAT_FRIEND: {
                        Intent intent = U.getActivityUtils().getLaunchIntentForPackage("com.tencent.mm");
                        if (intent != null && null != intent.resolveActivity(U.app().getPackageManager())) {
                            shareUrl(SharePlatform.WEIXIN_CIRCLE);
                        } else {
                            U.getToastUtil().showShort("未安装微信");
                        }
                    }
                    break;
                    default:
                        break;
                }
            }
        });

        //SkrKouLingUtils.genNormalJoinGrabGameKouling
        //SkrKouLingUtils.genJoinDoubleGameKouling
        //SkrKouLingUtils.genJoinMicRoomKouling
        //SkrKouLingUtils.genJoinRelayRoomKouling
        ApiMethods.subscribe(mInviteCallBack.getKouLingTokenObservable(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    mKouLingToken = obj.getData().getString("token");
                } else {
                    U.getToastUtil().showShort(obj.getErrmsg());
                }
            }
        });
    }


    private void shareUrl(SharePlatform sharePlatform) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://test.app.inframe.mobi/room/invitation")
                .append("?skerId=").append(String.valueOf(MyUserInfoManager.INSTANCE.getUid()))
                .append("&code=");
        String code = String.valueOf(mKouLingToken);
        try {
            code = URLEncoder.encode(code, "utf-8");
        } catch (UnsupportedEncodingException e) {

        }
        sb.append(code);
        String mUrl = ApiManager.getInstance().findRealUrlByChannel(sb.toString());

        UMWeb web = new UMWeb(mUrl);
        if (sharePlatform == SharePlatform.WEIXIN_CIRCLE) {
            web.setThumb(new UMImage(getActivity(), R.drawable.share_app_weixin_circle_icon));
        } else {
            web.setThumb(new UMImage(getActivity(), "http://res-static.inframe.mobi/common/skr_logo2.png"));
        }

        web.setTitle(mInviteCallBack.getShareTitle());
        web.setDescription(mInviteCallBack.getShareDes());

        switch (sharePlatform) {
            case QQ:
                new ShareAction(getActivity()).withMedia(web)
                        .setPlatform(SHARE_MEDIA.QQ)
                        .share();
                break;
            case QZONE:
                new ShareAction(getActivity()).withMedia(web)
                        .setPlatform(SHARE_MEDIA.QZONE)
                        .share();
                break;
            case WEIXIN:
                new ShareAction(getActivity()).withMedia(web)
                        .setPlatform(SHARE_MEDIA.WEIXIN)
                        .share();
                break;

            case WEIXIN_CIRCLE:
                new ShareAction(getActivity()).withMedia(web)
                        .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                        .share();
                break;
        }
    }

    private void showShareDialog() {
        if (mInviteFriendDialog == null) {
            mInviteFriendDialog = new InviteFriendDialog(getContext(), mKouLingToken, mInviteCallBack);
        }
        mInviteFriendDialog.show();
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mInviteCallBack = (IInviteCallBack) data;
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mInviteFriendDialog != null) {
            mInviteFriendDialog.dismiss(false);
        }
    }
}
