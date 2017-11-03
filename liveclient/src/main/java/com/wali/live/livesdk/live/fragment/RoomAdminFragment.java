package com.wali.live.livesdk.live.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.base.activity.BaseActivity;
import com.base.dialog.DialogUtils;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.network.Network;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.event.LiveRoomManagerEvent;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.manager.model.LiveRoomManagerModel;
import com.mi.live.data.query.model.MessageRule;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.FragmentEvent;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.LiveSdkActivity;
import com.wali.live.livesdk.live.adapter.RoomAdminItemRecyclerAdapter;
import com.wali.live.livesdk.live.api.GetRankListRequest;
import com.wali.live.livesdk.live.view.RoomSettingView;
import com.wali.live.livesdk.live.view.SlidingTabLayout;
import com.wali.live.proto.RankProto;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.watchsdk.adapter.CommonTabPagerAdapter;
import com.wali.live.watchsdk.personinfo.presenter.ForbidManagePresenter;
import com.wali.live.watchsdk.recipient.RecipientsSelectFragment;
import com.wali.live.watchsdk.recipient.adapter.RecipientsSelectRecyclerAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 房间管理
 * <p/>
 * Created by wuxiaoshan on 16-6-13.
 */
public class RoomAdminFragment extends RxFragment implements FragmentDataListener, ForbidManagePresenter.IForbidManageView {
    public static final String TAG = RoomAdminFragment.class.getSimpleName();

    public static final String KEY_ROOM_SEND_MSG_CONFIG = "key_room_send_msg_config";
    public static final String KEY_ROOM_ANCHOR_ID = "key_room_anchor_id";
    public static final String KEY_ROOM_IS_PRIVATE_LIVE = "key_room_is_private_live";
    //房间id
    public static final String INTENT_LIVE_ROOM_ID = "INTENT_LIVE_ROOM_ID";

    public static final String KEY_ONLY_SHOW_ADMIN_MANAGER_PAGE = "key_only_show_admin_manager_page";


    /**
     * 批量拉取的数量
     */
    private static final int PAGE_NUM = 50;
    /**
     * 房间管理TABID：管理员
     */
    public static final int TAB_ID_ADMIN = 1;
    /**
     * 房间挂历TABID：禁言列表
     */
    public static final int TAB_ID_BANSPEAKER = 2;
    /**
     * 房间设置的Tab
     */
    public static final int TAB_ID_ROOM_SETTING = 3;
    /**
     * 私密直播间里的邀请好友的Tab
     */
    public static final int TAB_ID_INVITE = 4;
    private BackTitleBar mTitleBar;
    private SlidingTabLayout mManagerTab;
    private ViewPager mSectionPager;
    private CommonTabPagerAdapter mManagerTabAdapter;
    private RecyclerView mAdminRv;
    private RecyclerView mBanspeakRv;
    private RoomAdminItemRecyclerAdapter mBanspeakRecyclerAdapter;
    private RoomAdminItemRecyclerAdapter mAdminRecyclerAdapter;
    private ProgressBar mLoadingView;
    private String mRoomId;
    private int mCurrentTabId;
    private long mAnchorId;
    private MessageRule mMsgRule;
    private boolean mOnlyShowAdminPage;
    /**
     * 是否为私密直播
     */
    private boolean mIsPrivateLive;
    private RoomSettingView mRoomSettingView;
    private ForbidManagePresenter mForbidManagePresenter;
    RoomAdminItemRecyclerAdapter.OnRoomStatusObserver mOnRoomStatusObserver;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupForbidManagePresenter();
        EventBus.getDefault().register(this);
        KeyboardUtils.hideKeyboardImmediately(getActivity());
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_room_admin, container, false);
    }

    @Override
    protected void bindView() {
        Bundle bundle = getArguments();
        mRoomId = bundle.getString(INTENT_LIVE_ROOM_ID, "");
        mMsgRule = (MessageRule) bundle.getSerializable(KEY_ROOM_SEND_MSG_CONFIG);
        mAnchorId = bundle.getLong(KEY_ROOM_ANCHOR_ID, 0);
        mIsPrivateLive = bundle.getBoolean(KEY_ROOM_IS_PRIVATE_LIVE, false);
        mOnlyShowAdminPage = bundle.getBoolean(KEY_ONLY_SHOW_ADMIN_MANAGER_PAGE, false);

        mTitleBar = (BackTitleBar) mRootView.findViewById(R.id.title_bar);
        if (!mOnlyShowAdminPage) {
            mTitleBar.getBackBtn().setText(getResources().getString(R.string.room_admin));
        } else {
            mTitleBar.getBackBtn().setText(getResources().getString(R.string.admin_list_title));
        }
        mTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                FragmentNaviUtils.popFragmentFromStack(getActivity());
            }
        });

        mAdminRv = new RecyclerView(getActivity());
        mAdminRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdminRecyclerAdapter = new RoomAdminItemRecyclerAdapter(RoomAdminItemRecyclerAdapter.DATA_TYPE_ADMIN, getActivity());
        mAdminRv.setAdapter(mAdminRecyclerAdapter);

        mBanspeakRv = new RecyclerView(getActivity());
        mBanspeakRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBanspeakRecyclerAdapter = new RoomAdminItemRecyclerAdapter(RoomAdminItemRecyclerAdapter.DATA_TYPE_BANSPEAKER, getActivity());
        mBanspeakRv.setAdapter(mBanspeakRecyclerAdapter);

        mRoomSettingView = new RoomSettingView(getActivity(), mMsgRule, mRoomId);

        mManagerTab = (SlidingTabLayout) mRootView.findViewById(R.id.manager_tab);
        if (!mOnlyShowAdminPage) {
            mManagerTab.setSelectedIndicatorColors(getResources().getColor(R.color.color_e5aa1e));
        } else {
            mManagerTab.setSelectedIndicatorColors(getResources().getColor(R.color.transparent));
            mManagerTab.setVisibility(View.GONE);
        }
        mManagerTab.setCustomTabView(R.layout.room_admin_slide_tab_view, R.id.tab_tv);
        mManagerTab.setDistributeMode(3);
        mManagerTab.setIndicatorWidth(DisplayUtils.dip2px(12));
        mManagerTab.setIndicatorBottomMargin(DisplayUtils.dip2px(4));

        mSectionPager = (ViewPager) mRootView.findViewById(R.id.section_pager);

        mManagerTabAdapter = new CommonTabPagerAdapter();
        mManagerTabAdapter.addView(getString(R.string.manager), mAdminRv);
        if (!mOnlyShowAdminPage) {
            mManagerTabAdapter.addView(getString(R.string.banspeaker_list), mBanspeakRv);
            mManagerTabAdapter.addView(getString(R.string.room_setting), mRoomSettingView);
        }

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
                if (tabId == TAB_ID_ADMIN) {
                    loadAdminData();
                } else if (tabId == TAB_ID_BANSPEAKER) {
                    mLoadingView.setVisibility(View.GONE);
                    loadBanSpeakerData();
                } else {
                    mLoadingView.setVisibility(View.GONE);
                }
                mCurrentTabId = tabId;
                MyLog.d(TAG, "currentTab = " + mCurrentTabId);
                if (mCurrentTabId == TAB_ID_ADMIN) {
                    StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, StatisticsKey.KEY_ROOM_ADMIN_TAB_ADMIN, 1);
                } else if (mCurrentTabId == TAB_ID_BANSPEAKER) {
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
            public void onRemoveAdmin(final long adminId) {
                //网络判断
                if (!Network.hasNetwork((GlobalData.app()))) {
                    ToastUtils.showToast(R.string.network_unavailable);
                    return;
                }
                Observable.just(0)
                        .map(new Func1<Integer, Object>() {
                            @Override
                            public Object call(Integer integer) {
                                return LiveRoomCharacterManager.cancelManager(adminId, mRoomId);
                            }
                        })
                        .compose(bindUntilEvent(FragmentEvent.DESTROY))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                if (o == null) {
                                    return;
                                }
                                if (!(boolean) o) {
                                    ToastUtils.showToast(R.string.remove_manager_fail);
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                MyLog.e(TAG, throwable);
                                ToastUtils.showToast(R.string.remove_manager_fail);
                            }
                        });
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
        mAdminRecyclerAdapter.setOnRoomStatusObserver(mOnRoomStatusObserver);
        mBanspeakRecyclerAdapter.setOnRoomStatusObserver(mOnRoomStatusObserver);
        mLoadingView = (ProgressBar) mRootView.findViewById(R.id.loading_view);
        mLoadingView.bringToFront();
        mCurrentTabId = mIsPrivateLive ? TAB_ID_INVITE : TAB_ID_ADMIN;
        if (mCurrentTabId == TAB_ID_ADMIN) {
            loadAdminData();
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
            mBanspeakRecyclerAdapter.addData(LiveRoomCharacterManager.getInstance().getSpeakerBanList());
        } else if (mBanspeakRecyclerAdapter.getDataList().size() != LiveRoomCharacterManager.getInstance().getSpeakerBanList().size()) {
            mBanspeakRecyclerAdapter.clearData();
            mBanspeakRecyclerAdapter.addData(LiveRoomCharacterManager.getInstance().getSpeakerBanList());
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
                    LiveRoomCharacterManager.getInstance().banSpeaker(user, false);
                    mBanspeakRecyclerAdapter.removeData(targetId);
                } else {
                    ToastUtils.showToast(R.string.cancel_banspeaker_fail);
                }
            }
        }
    }

    @Override
    public void onKickViewerDone(long targetId, int errCode) {
        MyLog.w(TAG, "onkickViewerDone targetId=" + targetId + ", errCode=" + errCode);
    }

    @Override
    public void onBlockViewer(long targetId, int errCode) {
        MyLog.w(TAG, "onBlockViewer targetId=" + targetId + ", errCode=" + errCode);
        if (errCode == ForbidManagePresenter.ERR_CODE_SUCCESS) {
            int index = -1;
            for (int i = 0; i < LiveRoomCharacterManager.getInstance().getSpeakerBanList().size(); i++) {
                User banSpeaker = LiveRoomCharacterManager.getInstance().getSpeakerBanList().get(i);
                if (banSpeaker.getUid() == targetId) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                LiveRoomCharacterManager.getInstance().getSpeakerBanList().get(index).setIsBlock(true);
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
        if (!mOnlyShowAdminPage) {
            if (!Network.hasNetwork((GlobalData.app()))) {
                ToastUtils.showToast(R.string.network_unavailable);
            } else {
                mRoomSettingView.settingChangeNotify();
            }
        }
        return false;
    }

    /**
     * 加载管理员数据
     *
     * @return
     */
    private void loadAdminData() {
        //网络判断
        if (!Network.hasNetwork((GlobalData.app()))) {
            ToastUtils.showToast(R.string.network_unavailable);
            return;
        }
        mLoadingView.setVisibility(View.VISIBLE);
        mAdminRecyclerAdapter.clearData();

        Observable.just(0)
                .map(new Func1<Integer, Long>() {
                    @Override
                    public Long call(Integer integer) {
                        return getTop1();
                    }
                })
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        loadAdminData((Long) o);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "get top1 error", throwable);
                        loadAdminData(0);
//                        mLoadingView.setVisibility(View.GONE);
                    }
                });


    }

    /**
     * 加载管理员数据
     *
     * @param top1Uid 榜一用户ID
     */
    private void loadAdminData(long top1Uid) {
        List<Long> uidList = new ArrayList<>();
        //在管理员列表增加top1用户
        Long top1 = LiveRoomCharacterManager.getInstance().getTop1Uuid(mAnchorId);
        if (top1Uid != 0) {
            if (top1 == null || top1 != top1Uid) {
                LiveRoomCharacterManager.getInstance().setTopRank(mAnchorId, top1Uid);
            }
            uidList.add(top1Uid);
        } else if (top1 != null) {
            uidList.add(top1);
            top1Uid = top1;
        }
        List<LiveRoomManagerModel> roomManagerList = LiveRoomCharacterManager.getInstance().getRoomManagers();
        if (roomManagerList != null && roomManagerList.size() > 0) {
            for (int i = 0; i < roomManagerList.size(); i++) {
                if (top1Uid != roomManagerList.get(i).uuid)
                    uidList.add(roomManagerList.get(i).uuid);

            }
        }
        if (uidList.size() > 0) {
            asyncGetAdminUser(uidList);
        } else {
            mLoadingView.setVisibility(View.GONE);
        }
    }

    /**
     * 获取top1用户
     *
     * @return
     */
    public static long getTop1() {
        List<Long> rankThrees = new ArrayList<>();
        RankProto.GetRankListResponse response = new GetRankListRequest().syncRsp();
        if (response != null) {
            List<RankProto.RankItem> rankItems = response.getItemsList();
            if (rankItems != null && rankItems.size() > 0) {
                for (RankProto.RankItem rankItem : rankItems) {
                    rankThrees.add(rankItem.getUuid());
                }
                return rankThrees.get(0);
            }
        }
        return 0;
    }

    /**
     * 异步从服务器拉取管理员数据
     *
     * @param uidList
     */
    private void asyncGetAdminUser(final List<Long> uidList) {
        Observable.just(0)
                .map(new Func1<Integer, List<User>>() {
                    @Override
                    public List<User> call(Integer i) {
                        List<User> userList = new ArrayList<User>();
                        if (uidList != null) {
                            for (Long uuid : uidList) {
                                User user = UserInfoManager.getUserInfoByUuid(uuid, false);
                                if (user != null) {
                                    userList.add(user);
                                }
                            }
                        }
                        return userList;
                    }
                })
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        mLoadingView.setVisibility(View.GONE);
                        if (o != null) {
                            mAdminRecyclerAdapter.addDataList((List<User>) o);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mLoadingView.setVisibility(View.GONE);
                        MyLog.e(TAG, "get user info error", throwable);
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomAdminItemRecyclerAdapter.RoomAdminAddAdminEvent event) {
        onClickAddAdmin();
    }

    private void onClickAddAdmin() {
        if (LiveRoomCharacterManager.getInstance().getRoomManagers() != null && LiveRoomCharacterManager.getInstance().getRoomManagers().size() >= LiveRoomCharacterManager.MANAGER_CNT) {
            ToastUtils.showToast(getResources().getString(R.string.manager_max_err, LiveRoomCharacterManager.MANAGER_CNT));
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString(RecipientsSelectFragment.SELECT_TITLE, getResources().getString(R.string.add_manager));
        bundle.putInt(RecipientsSelectFragment.SELECT_MODE, RecipientsSelectFragment.SELECT_MODE_SINGLE_SELECT);
        bundle.putBoolean(RecipientsSelectFragment.INTENT_SHOW_BOTH_WAY, false);
        bundle.putInt(RecipientsSelectFragment.DATA_TYPE, RecipientsSelectRecyclerAdapter.ITEM_TYPE_MANAGER);
        bundle.putBoolean(RecipientsSelectFragment.INTENT_ENABLE_SEARCH, true);
        bundle.putString(RecipientsSelectFragment.INTENT_LIVE_ROOM_ID, mRoomId);
        bundle.putInt(RecipientsSelectFragment.KEY_REQUEST_CODE, LiveSdkActivity.REQUEST_CODE_PICK_MANAGER);
        FragmentNaviUtils.addFragment((BaseActivity) getActivity(), R.id.main_act_container, RecipientsSelectFragment.class, bundle, true, true, true);

    }

    @Override
    public void onDestroy() {
        MyLog.d(TAG, "onDestroy : unregister eventbus");
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 处理添加/取消管理员事件回调
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LiveRoomManagerEvent event) {
        if (event != null && event.reqResult && mCurrentTabId == TAB_ID_ADMIN) {
            List<LiveRoomManagerModel> managerList = event.managerList;
            if (managerList != null && managerList.size() > 0) {
                if (event.managerEnable) {
                    LongSparseArray<Integer> uidMap = new LongSparseArray<>();
                    if (mAdminRecyclerAdapter.getDataList() != null) {
                        for (int i = 0; i < mAdminRecyclerAdapter.getDataList().size(); i++) {
                            User user = mAdminRecyclerAdapter.getDataList().get(i);
                            uidMap.put(user.getUid(), 1);
                        }
                    }
                    List<Long> addUidList = new ArrayList<>();
                    for (int i = 0; i < managerList.size(); i++) {
                        LiveRoomManagerModel manager = managerList.get(i);
                        if (uidMap.get(manager.uuid) == null) {
                            addUidList.add(manager.uuid);
                        }
                    }
                    if (addUidList.size() > 0) {
                        asyncGetAdminUser(addUidList);
                    }
                } else {
                    for (int i = 0; i < managerList.size(); i++) {
                        if (!LiveRoomCharacterManager.getInstance().isTopRank(mAnchorId, managerList.get(i).uuid)) {
                            mAdminRecyclerAdapter.removeData(managerList.get(i).uuid);
                        } else {
                            mAdminRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }

            }
        }
    }

}


