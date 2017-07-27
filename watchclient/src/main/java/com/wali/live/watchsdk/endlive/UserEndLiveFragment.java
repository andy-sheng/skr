package com.wali.live.watchsdk.endlive;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.fragment.BaseEventBusFragment;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.span.SpanUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.endlive.presenter.UserEndLivePresenter;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @module 新版直播结束页面 （用户界面）
 */
public class UserEndLiveFragment extends BaseEventBusFragment implements View.OnClickListener {
    private static final String TAG = UserEndLiveFragment.class.getSimpleName();
    private static final int REQUEST_CODE = GlobalData.getRequestCode();
    private static final String EXTRA_OWNER_ID = "extra_owner_id";
    private static final String EXTRA_ROOM_ID = "extra_room_id";
    private static final String EXTRA_AVATAR_TS = "extra_avatar_ts";
    private static final String EXTRA_OWNER = "extra_owner";
    private static final String EXTRA_VIEWER = "extra_viewer";
    private static final String EXTRA_LIVE_TYPE = "extra_live_type";
    private static final String EXTRA_TIME = "extra_time";
    private static final String EXTRA_TICKET = "extra_ticket";
    private static final String EXTRA_ENTER_TYPE = "extra_enter_type";
    private static final String EXTRA_ENTER_NICK_NAME = "extra_enter_nick_name";
    private static final String EXTRA_ROOMINFO_LIST = "extra_room_info_list";
    private static final String EXTRA_ROOMINFO_POSITION = "extra_roominfo_position";

    public static final String ENTER_TYPE_LIVE_END = "enter_type_live_end";     //进入房间后，正常直播结束
    public static final String ENTER_TYPE_LATE = "enter_type_late";             //进入房间时 已结束
    public static final String ENTER_TYPE_REPLAY = "enter_type_replay";         //回放结束
    public static final int COUNT_DOWN_TIME = 5;

    private SimpleDraweeView mAvatarBgDv;     //主播背景图
    private BaseImageView mAvatarIv;          //主播头像
    private TextView mNameTv;                 //主播名称
    private TextView mFollowTv;               //关注按钮
    private TextView mViewerTv;               //多少人看过
    private TextView mHomePageTv;             //前往首页
    private TextView mTimeHourTv;             //观看时长
    private TextView mTimeMinuteTv;
    private TextView mTimeSecondTv;
    private TextView mTicketTv;
    private TextView mHintTv;
    private TextView mFollowHintTv;
    private TextView mLiveIdTv;
    private LinearLayout mTimeContainer;
    private LinearLayout mNextRoomContainer;
    private TextView mNextRoomTv;
    private long mHour;
    private long mMinute;
    private long mSecond;

    private int mLiveType = LiveManager.TYPE_LIVE_PUBLIC;
    private long mOwnerId;
    private String mOwnerName;
    private long mAvatarTs;
    private String mRoomId;
    private int mTicket;
    private long mTime;
    private String mEndType;
    private User mOwner;
    private int mViewerCnt;
    private int mRoomPosition;
    private ArrayList<RoomInfo> mRoomInfoList;
    private UserEndLivePresenter mUserEndLivePresenter;
    private UserEndLivePresenter.IUserEndLiveView mUserEndLiveView = new UserEndLivePresenter.IUserEndLiveView() {
        @Override
        public void onFollowRefresh() {
            followResult(mOwner.isFocused());
        }

        @Override
        public void onCountDown(int time) {
            String plainText = getResources().getQuantityString(R.plurals.endlive_next_room_hint, time, time);
            SpannableStringBuilder ssb = new SpannableStringBuilder(plainText);
            int start = plainText.indexOf(String.valueOf(time));
            int end = start + String.valueOf(time).length();
            ssb.setSpan(new AbsoluteSizeSpan(48), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int color = ContextCompat.getColor(getActivity(), R.color.color_ff2966);
            ssb.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mNextRoomTv.setText(ssb);
        }

        @Override
        public void onNextRoom() {
            WatchSdkActivity.openActivity(getActivity(), mRoomInfoList, mRoomPosition);
        }
    };

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.end_live_layout_user, container, false);
    }

    @Override
    protected void bindView() {
        KeyboardUtils.hideKeyboard(getActivity());
        initFromArguments();
        initView();
        initPresenter();
        initData();
    }

    private void initView() {
        mAvatarBgDv = $(R.id.avatar_bg_dv);
        mAvatarIv = $(R.id.avatar_iv);
        mViewerTv = $(R.id.viewer_tv);
        mNameTv = $(R.id.name_tv);
        mFollowTv = $(R.id.follow_tv);
        mHomePageTv = $(R.id.home_page_tv);

        mTimeHourTv = $(R.id.time_hour_tv);
        mTimeMinuteTv = $(R.id.time_minute_tv);
        mTimeSecondTv = $(R.id.time_second_tv);
        mHintTv = $(R.id.hint_tv);

        mTicketTv = $(R.id.ticket_tv);
        mFollowHintTv = $(R.id.follow_hint_tv);
        mLiveIdTv = $(R.id.live_id_tv);
        mTimeContainer = $(R.id.hint_time_container);

        mNextRoomContainer = $(R.id.next_room_container);
        mNextRoomTv = $(R.id.next_room_tv);

        //click事件
        $click(mFollowTv, this);
        $click(mHomePageTv, this);
    }

    private void initFromArguments() {
        MyLog.w(TAG, "initFromArguments");
        Bundle arguments = getArguments();
        if (arguments != null) {
            mOwner = (User) arguments.getSerializable(EXTRA_OWNER);
            mLiveType = arguments.getInt(EXTRA_LIVE_TYPE, 0);
            mOwnerId = arguments.getLong(EXTRA_OWNER_ID, 0);
            mAvatarTs = arguments.getLong(EXTRA_AVATAR_TS, 0);
            mOwnerName = arguments.getString(EXTRA_ENTER_NICK_NAME, "");
            mRoomId = arguments.getString(EXTRA_ROOM_ID, "");
            mTicket = arguments.getInt(EXTRA_TICKET, 0);
            mTime = arguments.getLong(EXTRA_TIME, 0);
            mEndType = arguments.getString(EXTRA_ENTER_TYPE, "");
            mViewerCnt = arguments.getInt(EXTRA_VIEWER, 0);
            mLiveType = arguments.getInt(EXTRA_LIVE_TYPE, 0);
            mRoomInfoList = arguments.getParcelableArrayList(EXTRA_ROOMINFO_LIST);
            mRoomPosition = arguments.getInt(EXTRA_ROOMINFO_POSITION);
        }
    }

    public void initData() {
        AvatarUtils.loadAvatarByUidTs(mAvatarIv, mOwnerId, mAvatarTs, true);
        AvatarUtils.loadAvatarByUidTs(mAvatarBgDv, mOwnerId, mAvatarTs, AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false, true);
        mNameTv.setText(TextUtils.isEmpty(mOwnerName) ? String.valueOf(mOwnerId) : mOwnerName);
        mLiveIdTv.setText(getString(R.string.mi_live_accounts) + ":" + mOwnerId);
        mHour = mTime / (60 * 60 * 1000);
        mMinute = (mTime - mHour * 60 * 60 * 1000) / (60 * 1000);
        mSecond = (mTime - mHour * 60 * 60 * 1000 - mMinute * 60 * 1000) / 1000;

        switch (mEndType) {
            case ENTER_TYPE_REPLAY:
                mTimeContainer.setVisibility(View.GONE);
                mTicketTv.setVisibility(View.GONE);
                mHintTv.setText(getString(R.string.live_end_replay_hint));
                break;
            case ENTER_TYPE_LIVE_END:
                if (mHour > 0) {
                    mTimeHourTv.setText(SpanUtils.addColorSpan(String.valueOf(mHour),
                            String.format(getString(R.string.live_end_time_hours), String.valueOf(mHour)),
                            R.color.text_color_e5aa1c,
                            R.color.black));
                    mTimeMinuteTv.setText(SpanUtils.addColorSpan(String.valueOf(mMinute),
                            String.format(getString(R.string.live_end_time_minute), String.valueOf(mMinute)),
                            R.color.text_color_e5aa1c,
                            R.color.black));
                } else {
                    mTimeHourTv.setVisibility(View.GONE);
                    if (mMinute > 0) {
                        mTimeMinuteTv.setText(SpanUtils.addColorSpan(String.valueOf(mMinute),
                                String.format(getString(R.string.live_end_time_minute), String.valueOf(mMinute)),
                                R.color.text_color_e5aa1c,
                                R.color.black));
                    } else {
                        mTimeMinuteTv.setVisibility(View.GONE);
                    }
                }
                mTimeSecondTv.setText(SpanUtils.addColorSpan(String.valueOf(mSecond),
                        String.format(getString(R.string.live_end_time_second), String.valueOf(mSecond)),
                        R.color.text_color_e5aa1c,
                        R.color.black));
                if (mTicket > 0) {
                    mTicketTv.setText(SpanUtils.addColorSpan(String.valueOf(mTicket), String.format(getString(R.string.endlive_user_ticket), String.valueOf(mTicket)),
                            R.color.text_color_e5aa1c,
                            R.color.black));
                } else {
                    mTicketTv.setVisibility(View.GONE);
                }
                mHintTv.setText(getString(R.string.endlive_share_user_hint));
                break;
            case ENTER_TYPE_LATE:
                mTimeContainer.setVisibility(View.GONE);
                mTicketTv.setVisibility(View.GONE);
                mHintTv.setText(getString(R.string.live_end_hint));
                break;
            default:
                mTimeContainer.setVisibility(View.GONE);
                mTicketTv.setVisibility(View.GONE);
                mHintTv.setText(getString(R.string.live_end_hint));
                break;
        }
        if (mViewerCnt <= 0) {
            mViewerTv.setVisibility(View.INVISIBLE);
        } else {
            mViewerTv.setText(getResources().getQuantityString(R.plurals.live_end_viewer_cnt,
                    mViewerCnt, mViewerCnt));
        }
        if (UserAccountManager.getInstance().getUuidAsLong() == mOwnerId) {
            mFollowTv.setVisibility(View.GONE);
            mFollowHintTv.setVisibility(View.INVISIBLE);
        } else {
            followResult(mOwner.isFocused());
        }
        if (mRoomInfoList == null || mRoomInfoList.size() <= 1) {
            mNextRoomContainer.setVisibility(View.GONE);
        } else {
            mNextRoomContainer.setVisibility(View.VISIBLE);
            mUserEndLivePresenter.nextRoom(COUNT_DOWN_TIME);
        }
    }

    private void initPresenter() {
        mUserEndLivePresenter = new UserEndLivePresenter(mOwner, mUserEndLiveView);
        mUserEndLivePresenter.getUser();
    }

    private void setFollowStatus() {
        mFollowTv.setVisibility(View.GONE);
        mFollowHintTv.setVisibility(View.INVISIBLE);
    }

    private void setUnFollowStatus() {
        mFollowTv.setClickable(true);
        mFollowTv.setOnClickListener(this);
    }

    private void finish() {
        getActivity().finish();
    }

    @Override
    public boolean onBackPressed() {
        finish();
        return true;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.home_page_tv) {
            finish();
        } else if (i == R.id.follow_tv) {
            if (mUserEndLivePresenter != null && AccountAuthManager.triggerActionNeedAccount(getActivity())) {
                mUserEndLivePresenter.follow(mRoomId);
            }
        }
    }

    /**
     * 关注与取消关注
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FollowOrUnfollowEvent event) {
        if (event == null) {
            MyLog.v(TAG, " onEventMainThread FollowOrUnfollowEvent event is null");
            return;
        }
        int type = event.eventType;
        if (mOwnerId == event.uuid) {
            if (type == FollowOrUnfollowEvent.EVENT_TYPE_FOLLOW) {
                setFollowStatus();
            } else if (type == FollowOrUnfollowEvent.EVENT_TYPE_UNFOLLOW) {
                setUnFollowStatus();
            }
        }
    }

    /**
     * 更新关注按钮UI
     */
    public void setFollowText() {
        setFollowStatus();
    }

    /**
     * 显示关注主播结果
     */
    public void followResult(boolean result) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            if (result) {
                setFollowText();
            } else {
                setUnFollowStatus();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUserEndLivePresenter != null) {
            mUserEndLivePresenter.destroy();
        }
    }

    /**
     * 打开直播结束页面
     *
     * @param activity Context
     * @param ownerId  主播ID
     * @param roomId   房间Id
     * @param avatarTs 时间戳
     * @param owner    主播
     * @param viewer   观众数
     * @param liveType 房间类型Live.proto里定义
     * @return
     */
    public static UserEndLiveFragment openFragment(FragmentActivity activity, long ownerId, String roomId,
                                                   long avatarTs, User owner, int viewer, int liveType, int ticket,
                                                   long time, String type, String nickName, ArrayList<RoomInfo> roomInfoList, int position) {
        if (activity == null || activity.isFinishing()) {
            MyLog.d(TAG, "openFragment activity state is illegal");
            return null;
        }
        Bundle bundle = getBundle(ownerId, roomId, avatarTs, owner, viewer,
                liveType, ticket, time, type, nickName, roomInfoList, position);
        UserEndLiveFragment userEndLiveFragment = (UserEndLiveFragment) FragmentNaviUtils.addFragment(activity, R.id.main_act_container,
                UserEndLiveFragment.class, bundle, true, false, true);
        return userEndLiveFragment;
    }

    public static Bundle getBundle(long ownerId, String roomId, long avatarTs, User owner, int viewer, int liveType,
                                   int ticket, long time, String type, String nickName, ArrayList<RoomInfo> roomInfoList, int position) {
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_OWNER_ID, ownerId);
        bundle.putString(EXTRA_ROOM_ID, roomId);
        bundle.putLong(EXTRA_AVATAR_TS, avatarTs);
        bundle.putSerializable(EXTRA_OWNER, owner);
        bundle.putBoolean(BaseFragment.PARAM_FORCE_PORTRAIT, true);
        bundle.putInt(EXTRA_VIEWER, viewer);
        bundle.putInt(EXTRA_LIVE_TYPE, liveType);
        bundle.putInt(EXTRA_TICKET, ticket);
        bundle.putLong(EXTRA_TIME, time);
        bundle.putString(EXTRA_ENTER_TYPE, type);
        bundle.putString(EXTRA_ENTER_NICK_NAME, nickName);
        bundle.putParcelableArrayList(EXTRA_ROOMINFO_LIST, roomInfoList);
        bundle.putInt(EXTRA_ROOMINFO_POSITION, position);
        return bundle;
    }
}
