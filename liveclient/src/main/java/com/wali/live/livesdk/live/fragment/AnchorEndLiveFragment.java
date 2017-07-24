package com.wali.live.livesdk.live.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.dialog.DialogUtils;
import com.base.fragment.MyRxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.date.DateTimeUtils;
import com.base.utils.span.SpanUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.user.User;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.fragment.presenter.AnchorEndlivePresenter;
import com.wali.live.proto.RankProto;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.watch.presenter.SnsShareHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @module 新版直播结束页面 （主播界面）
 */
public class AnchorEndLiveFragment extends MyRxFragment implements View.OnClickListener {
    private static final String TAG = AnchorEndLiveFragment.class.getSimpleName();
    private static final int REQUEST_CODE = GlobalData.getRequestCode();

    private static final String EXTRA_OWNER = "extra_owner";
    private static final String EXTRA_VIEWER_COUNT = "extra_viewer_count";
    private static final String EXTRA_DURATION = "extra_duration";
    private static final String EXTRA_NEW_FOLLOWER = "extra_new_follower";
    private static final String EXTRA_TICKET = "extra_ticket";
    private static final String EXTRA_ROOM_ID = "extra_room_id";
    private static final String EXTRA_LIVE_TYPE = "extra_live_type";
    private static final String EXTRA_GENERATE_HISTORY = "extra_generate_history";
    private static final String EXTRA_GENERATE_HISTORY_MSG = "extra_generate_history_msg";
    private static final String EXTRA_ADD_HISTORY = "extra_add_history";
    private static final String EXTRA_HISTORY_LIVE_COUNT = "extra_history_live_count";
    private static final String EXTRA_SHARE_URL = "extra_share_url";
    private static final String EXTRA_SHARE_TITLE = "extra_share_title";
    private static final String EXTRA_ENABLE_SHARE = "extra_enable_share";
    private static final String EXTRA_GENERATE_COVER_URL = "extra_generate_cover_url";
    private static final String EXTRA_LOCATION = "extra_location";

    private SimpleDraweeView mAvatarBg;
    private TextView mNameTv;
    private TextView mMiAccountTv;
    private TextView mLiveTimesTv;
    private TextView mDurationTv;
    private TextView mViewerTv;
    private TextView mNewFansTv;
    private TextView mTicketTv;
    private TextView mDeleteTv;
    private TextView mBackTv;

    private BaseImageView mAnchorIv;
    private SimpleDraweeView mFirstDv;
    private SimpleDraweeView mSecondDv;
    private SimpleDraweeView mThreeDv;

    private ImageView mShareSelectedIv;
    private View mShareContainer;

    private boolean mAllowShare;
    private User owner;
    private int mViewerCount;
    private long mDuration;
    private long mNewFollower;
    private long mTicket;
    private String mRoomId;
    private String mShareUrl;
    private String mShareTitle;
    private String mCoverUrl;
    private String mLocation;
    private boolean mIsPrivate;
    private int mLiveType;
    private boolean mGenerateHistorySucc;
    private String mGenerateHistoryMsg;
    public boolean mIsAddHistory;
    public int mHistoryLiveCount;

    private final SimpleDraweeView[] mTopThree = new SimpleDraweeView[3];
    protected AnchorEndlivePresenter mEndlivePresenter;
    private AnchorEndlivePresenter.IAnchorEndLiveView mAnchorEndLiveView = new AnchorEndlivePresenter.IAnchorEndLiveView() {
        @Override
        public void onGetTopThree(List<RankProto.RankUser> list) {
            setTopThree(list);
        }
    };

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.end_live_layout_anchor, container, false);
    }

    @Override
    protected void bindView() {
        KeyboardUtils.hideKeyboard(getActivity());
        initFromArguments();
        initView();
        initData();
        initAvatar();
        initPresenter();
    }

    private void initPresenter() {
        mEndlivePresenter = new AnchorEndlivePresenter(mAnchorEndLiveView);
        mEndlivePresenter.getTopThree(mRoomId);
    }

    private void initAvatar() {
        AvatarUtils.loadAvatarByUidTs(mAvatarBg, owner.getUid(), owner.getAvatar(), AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false, true);
        AvatarUtils.loadAvatarByUidTs(mAnchorIv, owner.getUid(), owner.getAvatar(), AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, true, false);
    }

    private void initData() {
        mNameTv.setText(owner.getNickname());
        mMiAccountTv.setText(getString(R.string.mi_live_accounts) + ":" + owner.getUid());
        mDurationTv.setText(DateTimeUtils.formatVideoTime(mDuration));
        mViewerTv.setText(mViewerCount + "");
        mNewFansTv.setText(mNewFollower + "");
        mTicketTv.setText(mTicket + "");
        mLiveTimesTv.setText(SpanUtils.addColorSpan(String.valueOf(mHistoryLiveCount),
                getResources().getQuantityString(R.plurals.live_end_history_live_count, mHistoryLiveCount, mHistoryLiveCount),
                R.color.text_color_e5aa1c,
                R.color.black));

        //根据准备直播页面的CheckedTextView来确定是否显示删除按钮
        if (mIsAddHistory) {
            mDeleteTv.setVisibility(View.VISIBLE);
            MyLog.d(TAG, " generateHistory = " + mGenerateHistorySucc);
            if (mGenerateHistorySucc) {
                mDeleteTv.setOnClickListener(this);
            } else {
                mDeleteTv.setText(mGenerateHistoryMsg);
            }
        } else {
            mDeleteTv.setVisibility(View.INVISIBLE);
        }
        if (!mAllowShare) {
            mShareContainer.setVisibility(View.GONE);
        } else {
            boolean shareSelectedState = PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.ENDSHARE_SELECTED_STATE, true);
            mShareSelectedIv.setSelected(shareSelectedState);
        }
    }

    private void initView() {
        initWidget();
        initClick();
    }

    private void initClick() {
        $click(mBackTv, this);
        $click(mShareContainer, this);
    }

    private void initWidget() {
        mAvatarBg = $(R.id.avatar_bg_dv);
        mNameTv = $(R.id.name_tv);
        mMiAccountTv = $(R.id.mi_account_tv);
        mLiveTimesTv = $(R.id.times_tv);
        mDurationTv = $(R.id.duration_tv);
        mViewerTv = $(R.id.viewer_tv);
        mNewFansTv = $(R.id.new_fans_tv);
        mTicketTv = $(R.id.ticket_count_tv);
        mDeleteTv = $(R.id.delete_tv);
        mBackTv = $(R.id.back_tv);

        mAnchorIv = $(R.id.avatar_iv);
        mFirstDv = $(R.id.first_dv);
        mSecondDv = $(R.id.second_dv);
        mThreeDv = $(R.id.three_dv);

        mTopThree[0] = mFirstDv;
        mTopThree[1] = mSecondDv;
        mTopThree[2] = mThreeDv;

        mShareContainer = $(R.id.share_container);
        mShareSelectedIv = $(R.id.share_friends_iv);
    }

    private void initFromArguments() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            owner = (User) arguments.getSerializable(EXTRA_OWNER);
            mViewerCount = arguments.getInt(EXTRA_VIEWER_COUNT, 0);
            mDuration = arguments.getLong(EXTRA_DURATION, 0);
            mShareUrl = arguments.getString(EXTRA_SHARE_URL, "");
            mShareTitle = arguments.getString(EXTRA_SHARE_TITLE, "");
            mLocation = arguments.getString(EXTRA_LOCATION, "");
            mCoverUrl = arguments.getString(EXTRA_GENERATE_COVER_URL, "");
            mNewFollower = arguments.getLong(EXTRA_NEW_FOLLOWER, 0);
            mTicket = arguments.getLong(EXTRA_TICKET, 0);
            mRoomId = arguments.getString(EXTRA_ROOM_ID, "");
            mLiveType = arguments.getInt(EXTRA_LIVE_TYPE, 0);
            mIsPrivate = mLiveType == LiveManager.TYPE_LIVE_PRIVATE;

            mGenerateHistorySucc = arguments.getBoolean(EXTRA_GENERATE_HISTORY, false);
            mGenerateHistoryMsg = arguments.getString(EXTRA_GENERATE_HISTORY_MSG, "");
            mIsAddHistory = arguments.getBoolean(EXTRA_ADD_HISTORY, false);
            mHistoryLiveCount = arguments.getInt(EXTRA_HISTORY_LIVE_COUNT, 0);
            mAllowShare = arguments.getBoolean(EXTRA_ENABLE_SHARE, false);
        }
    }

    private void processShare() {
        if (mShareContainer.getVisibility() == View.VISIBLE) {
            PreferenceUtils.setSettingBoolean(GlobalData.app(), PreferenceKeys.ENDSHARE_SELECTED_STATE, mShareSelectedIv.isSelected());
            if (mShareSelectedIv.isSelected()) {
                SnsShareHelper.getInstance().shareToSns(-1, mShareUrl, mCoverUrl, mLocation, mShareTitle,
                        MyUserInfoManager.getInstance().getUser());
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        finish();
        return true;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.back_tv) {
            finish();
        } else if (i == R.id.delete_tv) {
            DialogUtils.showNormalDialog(getActivity(),
                    0, R.string.confirm_delete_replay,
                    R.string.ok, R.string.cancel,
                    new DialogUtils.IDialogCallback() {
                        @Override
                        public void process(DialogInterface dialogInterface, int i) {
                            mEndlivePresenter.deleteHistory(mRoomId);
                            getActivity().finish();
                        }
                    }, null);
        } else if (i == R.id.share_container) {
            mShareSelectedIv.setSelected(!mShareSelectedIv.isSelected());
        }
    }

    private void finish() {
        processShare();
        getActivity().finish();
    }

    public void setTopThree(List<RankProto.RankUser> topThree) {
        List<UserListData> userList = new ArrayList<>();
        for (RankProto.RankUser rankUser : topThree) {
            userList.add(new UserListData(rankUser));
        }
        for (int i = 0; i < mTopThree.length; i++) {
            if (topThree.size() > i) {
                AvatarUtils.loadAvatarByUidTs(mTopThree[i], topThree.get(i).getUuid(),
                        topThree.get(i).getAvatar(), true);
            } else {
                mTopThree[i].setImageResource(R.drawable.endlive_qiuzhan);
            }
        }
    }

    public static AnchorEndLiveFragment openFragment(FragmentActivity activity, User owner,
                                                     String shareUrl,
                                                     String shareTitle, String coverUrl,
                                                     String location, int viewerCount,
                                                     long duration, long newFollower,
                                                     long ticket, String roomId, int liveType,
                                                     boolean mGenerateHistorySucc,
                                                     String mGenerateHistoryMsg,
                                                     boolean mIsAddHistory, int mHistoryLiveCount,
                                                     boolean allowShare) {
        if (activity == null || activity.isFinishing()) {
            MyLog.d(TAG, "openFragment activity state is illegal");
            return null;
        }
        Bundle bundle = getBundle(owner, shareUrl, shareTitle, coverUrl, location,
                viewerCount, duration, newFollower, ticket,
                roomId, liveType, mGenerateHistorySucc,
                mGenerateHistoryMsg, mIsAddHistory, mHistoryLiveCount, allowShare);

        return (AnchorEndLiveFragment) FragmentNaviUtils.addFragment(activity, R.id.main_act_container,
                AnchorEndLiveFragment.class, bundle, true, false, true);
    }

    public static Bundle getBundle(User owner, String shareUrl, String shareTitle,
                                   String coverUrl, String location, int viewerCount,
                                   long duration, long newFollower, long ticket,
                                   String roomId, int liveType, boolean generateHistorySucc,
                                   String generateHistoryMsg, boolean isAddHistory,
                                   int historyLiveCount, boolean allowShare) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_VIEWER_COUNT, viewerCount);
        bundle.putLong(EXTRA_DURATION, duration);
        bundle.putSerializable(EXTRA_OWNER, owner);
        bundle.putString(EXTRA_SHARE_URL, shareUrl);
        bundle.putString(EXTRA_SHARE_TITLE, shareTitle);
        bundle.putString(EXTRA_GENERATE_COVER_URL, coverUrl);
        bundle.putString(EXTRA_LOCATION, location);
        bundle.putLong(EXTRA_NEW_FOLLOWER, newFollower);
        bundle.putLong(EXTRA_TICKET, ticket);
        bundle.putString(EXTRA_ROOM_ID, roomId);
        bundle.putInt(EXTRA_LIVE_TYPE, liveType);
        bundle.putBoolean(EXTRA_GENERATE_HISTORY, generateHistorySucc);
        bundle.putString(EXTRA_GENERATE_HISTORY_MSG, generateHistoryMsg);
        bundle.putBoolean(EXTRA_ADD_HISTORY, isAddHistory);
        bundle.putInt(EXTRA_HISTORY_LIVE_COUNT, historyLiveCount);
        bundle.putBoolean(EXTRA_ENABLE_SHARE, allowShare);
        return bundle;
    }
}
