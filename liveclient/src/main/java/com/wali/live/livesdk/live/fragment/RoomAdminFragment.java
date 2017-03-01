package com.wali.live.livesdk.live.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.base.dialog.DialogUtils;
import com.wali.live.fragment.FragmentDataListener;
import com.wali.live.fragment.MyRxFragment;
import com.wali.live.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.network.Network;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.manager.LiveRoomCharactorManager;
import com.mi.live.data.query.model.MessageRule;
import com.mi.live.data.user.User;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.adapter.CommonTabPagerAdapter;
import com.wali.live.livesdk.live.adapter.RoomAdminItemRecyclerAdapter;
import com.wali.live.livesdk.live.view.RoomSettingView;
import com.wali.live.livesdk.live.view.SlidingTabLayout;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.watchsdk.personinfo.presenter.ForbidManagePresenter;

/**
 * 房间管理
 * <p/>
 * Created by wuxiaoshan on 16-6-13.
 */
public class RoomAdminFragment extends MyRxFragment implements FragmentDataListener, ForbidManagePresenter.IForbidManageView {

    public static final String TAG = RoomAdminFragment.class.getSimpleName();
    /**
     * 批量拉取的数量
     */
    private static final int PAGE_NUM = 50;
    /**
     * 房间挂历TABID：禁言列表
     */
    public static final int TAB_ID_BANSPEAKER = 1;
    /**
     * 房间设置的Tab
     */
    public static final int TAB_ID_ROOM_SETTING = 2;
    /**
     * 私密直播间里的邀请好友的Tab
     */
    public static final int TAB_ID_INVITE = 3;
    private BackTitleBar mTitleBar;
    private SlidingTabLayout mManagerTab;
    private ViewPager mSectionPager;
    private CommonTabPagerAdapter mManagerTabAdapter;
    private RecyclerView mBanspeakRv;
    private RoomAdminItemRecyclerAdapter mBanspeakRecyclerAdapter;
    private ProgressBar mLoadingView;
    private String mRoomId;
    private int mCurrentTabId;
    private long mAnchorId;
    private MessageRule mMsgRule;
    /**
     * 是否为私密直播
     */
    private boolean mIsPrivateLive;
    private RoomSettingView mRoomSettingView;
    private ForbidManagePresenter mForbidManagePresenter;
    RoomAdminItemRecyclerAdapter.OnRoomStatusObserver mOnRoomStatusObserver;
    public static final String KEY_ROOM_SEND_MSG_CONFIG = "key_room_send_msg_config";
    public static final String KEY_ROOM_ANCHOR_ID = "key_room_anchor_id";
    public static final String KEY_ROOM_IS_PRIVATE_LIVE = "key_room_is_private_live";
    //房间id
    public static final String INTENT_LIVE_ROOM_ID = "INTENT_LIVE_ROOM_ID";

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupForbidManagePresenter();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_room_admin, container, false);
    }

    @Override
    protected void bindView() {
        Bundle bundle = getArguments();
        mRoomId = bundle.getString(INTENT_LIVE_ROOM_ID);
        mMsgRule = (MessageRule) bundle.getSerializable(KEY_ROOM_SEND_MSG_CONFIG);
        mAnchorId = bundle.getLong(KEY_ROOM_ANCHOR_ID, 0);
        mIsPrivateLive = bundle.getBoolean(KEY_ROOM_IS_PRIVATE_LIVE);
        mTitleBar = (BackTitleBar) mRootView.findViewById(R.id.title_bar);
        mTitleBar.getBackBtn().setText(getResources().getString(R.string.room_admin));
        mTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                FragmentNaviUtils.popFragmentFromStack(getActivity());
            }
        });
        mBanspeakRv = new RecyclerView(getActivity());
        mBanspeakRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBanspeakRecyclerAdapter = new RoomAdminItemRecyclerAdapter(RoomAdminItemRecyclerAdapter.DATA_TYPE_BANSPEAKER, getActivity());
        mBanspeakRv.setAdapter(mBanspeakRecyclerAdapter);
        mRoomSettingView = new RoomSettingView(getActivity(), mMsgRule, mRoomId);
        mManagerTab = (SlidingTabLayout) mRootView.findViewById(R.id.manager_tab);
        mManagerTab.setSelectedIndicatorColors(getResources().getColor(R.color.color_e5aa1e));
        mManagerTab.setCustomTabView(R.layout.room_admin_slide_tab_view, R.id.tab_tv);
        mManagerTab.setDistributeMode(3);
        mManagerTab.setIndicatorWidth(DisplayUtils.dip2px(12));
        mManagerTab.setIndicatorBottomMargin(DisplayUtils.dip2px(4));
        mSectionPager = (ViewPager) mRootView.findViewById(R.id.section_pager);
        mManagerTabAdapter = new CommonTabPagerAdapter();
        mManagerTabAdapter.addView(getString(R.string.banspeaker_list), mBanspeakRv);
        mManagerTabAdapter.addView(getString(R.string.room_setting), mRoomSettingView);
        mSectionPager.setAdapter(mManagerTabAdapter);
        mManagerTab.setViewPager(mSectionPager);
        mManagerTab.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int tabId = position2TabId(position);
                // 如果标签页没变
                if (tabId == mCurrentTabId) {
                    return;
                }
                if (tabId == TAB_ID_BANSPEAKER) {
                    loadBanSpeakerData();
                }
                mCurrentTabId = tabId;
                MyLog.d(TAG, "currentTab = " + mCurrentTabId);
                if (mCurrentTabId == TAB_ID_BANSPEAKER) {
                    StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, StatisticsKey.KEY_ROOM_ADMIN_TAB_BANSPEAK, 1);
                } else if (mCurrentTabId == TAB_ID_ROOM_SETTING) {
                    StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, StatisticsKey.KEY_ROOM_ADMIN_TAB_ROOMSETTING, 1);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mOnRoomStatusObserver = new RoomAdminItemRecyclerAdapter.OnRoomStatusObserver() {
            @Override
            public void onRemoveAdmin(long adminId) {
            }

            @Override
            public void onRemoveForbidSpeak(long forbidId) {
                //网络判断
                if (!Network.hasNetwork((GlobalData.app()))) {
                    ToastUtils.showToast(R.string.network_unavailable);
                    return;
                }
                if (mForbidManagePresenter != null) {
                    long userId = UserAccountManager.getInstance().getUuidAsLong();
                    mForbidManagePresenter.cancelForbidSpeak(mRoomId, userId, userId, forbidId);
                }
            }

            @Override
            public void onBlockViewer(final long blockId) {
                //网络判断
                if (!Network.hasNetwork((GlobalData.app()))) {
                    ToastUtils.showToast(R.string.network_unavailable);
                    return;
                }
                if (mForbidManagePresenter != null) {
                    final long userId = UserAccountManager.getInstance().getUuidAsLong();
                    DialogUtils.showNormalDialog(getActivity(), 0, R.string.block_confirm_tips, R.string.block, R.string.cancle_operating, new DialogUtils.IDialogCallback() {
                        @Override
                        public void process(DialogInterface dialogInterface, int i) {
                            mForbidManagePresenter.blockViewer(userId, blockId);
                        }
                    }, null);
                }
            }
        };

        mBanspeakRecyclerAdapter.setOnRoomStatusObserver(mOnRoomStatusObserver);
        mLoadingView = (ProgressBar) mRootView.findViewById(R.id.loading_view);
        mCurrentTabId = mIsPrivateLive ? TAB_ID_INVITE : TAB_ID_BANSPEAKER;
        if (mCurrentTabId == TAB_ID_BANSPEAKER) {
            loadBanSpeakerData();
        }
        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, StatisticsKey.KEY_ENTER_ROOM_ADMIN_PAGE, 1);
    }

    /**
     * 根据viewPager的position返回TabId<br/>
     * TODO 当Tab类型变动时修改此方法
     *
     * @param position
     * @return
     */
    private int position2TabId(int position) {
        if (!mIsPrivateLive) {
            return position + 1;
        } else {
            switch (position) {
                case 0:
                    return TAB_ID_INVITE;
                default:
                    return position;
            }
        }
    }

    /**
     * 加载禁言人员列表
     */
    private void loadBanSpeakerData() {
        //网络判断
        if (!Network.hasNetwork((GlobalData.app()))) {
            ToastUtils.showToast(R.string.network_unavailable);
            return;
        }
        if (mBanspeakRecyclerAdapter.getDataList() == null) {
            mBanspeakRecyclerAdapter.addData(LiveRoomCharactorManager.getInstance().getSpeakerBanList());
        } else if (mBanspeakRecyclerAdapter.getDataList().size() != LiveRoomCharactorManager.getInstance().getSpeakerBanList().size()) {
            mBanspeakRecyclerAdapter.clearData();
            mBanspeakRecyclerAdapter.addData(LiveRoomCharactorManager.getInstance().getSpeakerBanList());
        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public boolean isStatusBarDark() {
        return true;
    }

    @Override
    public boolean isOverrideStatusBar() {
        return true;
    }

    private void setupForbidManagePresenter() {
        Activity activity = getActivity();
        if (activity != null && activity instanceof ForbidManagePresenter.IForbidManageProvider) {
            mForbidManagePresenter = ((ForbidManagePresenter.IForbidManageProvider) activity).provideForbidManagePresenter();
            if (mForbidManagePresenter != null) {
                mForbidManagePresenter.setForbidManageView(this);
            }
        }
    }

    @Override
    public void onForbidSpeakDone(User user, int errCode) {
        MyLog.w(TAG, "onForbidSpeakDone targetId=" + user.getUid() + ", errCode=" + errCode);
    }

    @Override
    public void onCancelForbidSpeakDone(long targetId, int errCode) {
        MyLog.w(TAG, "onCancelForbidSpeakDone targetId=" + targetId + ", errCode=" + errCode);
        if (errCode == ForbidManagePresenter.ERR_CODE_SUCCESS) {
            if (mCurrentTabId == TAB_ID_BANSPEAKER) {
                if (errCode == ForbidManagePresenter.ERR_CODE_SUCCESS) {
                    User user = new User();
                    user.setUid(targetId);
                    LiveRoomCharactorManager.getInstance().banSpeaker(user, false);
                    mBanspeakRecyclerAdapter.removeData(targetId);
                } else {
                    ToastUtils.showToast(R.string.cancel_banspeaker_fail);
                }
            }
        }
    }

    @Override
    public void onkickViewerDone(long targetId, int errCode) {
        MyLog.w(TAG, "onkickViewerDone targetId=" + targetId + ", errCode=" + errCode);
    }

    @Override
    public void onBlockViewer(long targetId, int errCode) {
        MyLog.w(TAG, "onBlockViewer targetId=" + targetId + ", errCode=" + errCode);
        if (errCode == ForbidManagePresenter.ERR_CODE_SUCCESS) {
            int index = -1;
            for (int i = 0; i < LiveRoomCharactorManager.getInstance().getSpeakerBanList().size(); i++) {
                User banSpeaker = LiveRoomCharactorManager.getInstance().getSpeakerBanList().get(i);
                if (banSpeaker.getUid() == targetId) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                LiveRoomCharactorManager.getInstance().getSpeakerBanList().get(index).setIsBlock(true);
            }
            if (mCurrentTabId == TAB_ID_BANSPEAKER) {
                User user = mBanspeakRecyclerAdapter.getDataByUserId(targetId);
                if (user != null) {
                    user.setIsBlock(true);
                }

                mBanspeakRecyclerAdapter.notifyDataSetChanged();
            }

        } else {
            ToastUtils.showToast(R.string.block_failed);
        }
    }

    public boolean onBackPressed() {
        //网络判断
        if (!Network.hasNetwork((GlobalData.app()))) {
            ToastUtils.showToast(R.string.network_unavailable);
        } else {
            mRoomSettingView.settingChangeNotify();
        }
        return false;
    }
}


