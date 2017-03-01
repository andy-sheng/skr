package com.wali.live.livesdk.live.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.dialog.DialogUtils;
import com.wali.live.fragment.BaseFragment;
import com.wali.live.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.span.SpanUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wali.live.base.BaseSdkActivity;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.user.User;
import com.mi.milink.sdk.base.CustomHandlerThread;
import com.wali.live.common.action.VideoAction;
import com.wali.live.common.keyboard.KeyboardUtils;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.task.IActionCallBack;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.schema.processor.WaliliveProcessor;

/**
 * Created by lan on 15-12-15.
 *
 * @module 直播结束页面
 */
public class EndLiveFragment extends BaseFragment implements View.OnClickListener, IActionCallBack {
    private static final String TAG = EndLiveFragment.class.getSimpleName();

    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    private static final String EXTRA_OWNER_ID = "extra_owner_id";
    private static final String EXTRA_ROOM_ID = "extra_room_id";
    private static final String EXTRA_AVATAR_TS = "extra_avatar_ts";
    private static final String EXTRA_VIEWER_CNT = "extra_viewer_cnt";
    private static final String EXTRA_TICKET = "extra_ticket";
    private static final String EXTRA_FROM = "extra_from";

    private static final String EXTRA_SHARE_URL = "extra_share_url";
    private static final String EXTRA_LOCATION = "extra_location";
    private static final String EXTRA_OWNER = "extra_owner";
    public static final String EXTRA_ADD_HISTORY = "extra_add_history";
    public static final String EXTRA_GENERATE_HISTORY = "extra_generate_history";
    public static final String EXTRA_GENERATE_HISTORY_MSG = "extra_generate_history_msg";
    public static final String EXTRA_TICKET_BUYER_COUNT = "extra_ticket_buyer_count";
    public static final String EXTRA_GENERATE_COVER_URL = "extra_generate_cover_url";
    public static final String EXTRA_GENERATE_LIVE_TITLE = "extra_generate_live_title";

    public static final String EXTRA_LIVE_TYPE = "extra_live_type";
    private static final String EXTRA_FAILURE = "extra_failure";

    private SimpleDraweeView mAvatarBg;
    private TextView mViewerTv;
    private TextView mTicketTv;
    private TextView mBackBtn;
    private TextView mDeleteBtn;

    private boolean mIsShowTicketView = true;

    private long mOwnerId;
    private String mRoomId;
    private long mAvatarTs;
    private int mViewerCnt;
    private int mTicket;
    private User mOwner;
    public boolean mIsAddHistory;
    // 是否是私密直播,同时保留LiveType变量
    private int mLiveType;
    private boolean mGenerateHistorySucc;
    private String mGenerateHistoryMsg;

    private boolean mIsFailure;

    CustomHandlerThread mHandlerThread = new CustomHandlerThread(TAG) {
        @Override
        protected void processMessage(Message message) {

        }
    };

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.end_live_layout, container, false);
    }

    @Override
    protected void bindView() {
        KeyboardUtils.hideKeyboardImmediately(getActivity());
        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        initData();
        initContentView();
    }

    private void initData() {
        Bundle bundle = getArguments();
        mOwnerId = bundle.getLong(EXTRA_OWNER_ID, -1);
        mRoomId = bundle.getString(EXTRA_ROOM_ID, "");
        mAvatarTs = bundle.getLong(EXTRA_AVATAR_TS, 0);
        mViewerCnt = bundle.getInt(EXTRA_VIEWER_CNT, 0);
        mTicket = bundle.getInt(EXTRA_TICKET, 0);
        mLiveType = bundle.getInt(EXTRA_LIVE_TYPE, 0);
        mIsFailure = bundle.getBoolean(EXTRA_FAILURE, false);
        mIsAddHistory = bundle.getBoolean(EXTRA_ADD_HISTORY, false);
        mGenerateHistorySucc = bundle.getBoolean(EXTRA_GENERATE_HISTORY, false);
        mGenerateHistoryMsg = bundle.getString(EXTRA_GENERATE_HISTORY_MSG, "");
        mOwner = (User) bundle.getSerializable(EXTRA_OWNER);
    }

    private void initContentView() {
        mAvatarBg = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_bg_dv);
        AvatarUtils.loadAvatarByUidTs(mAvatarBg, mOwnerId, mAvatarTs, AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false, true);

        mViewerTv = (TextView) mRootView.findViewById(R.id.viewer_tv);
        mViewerTv.setText(SpanUtils.addColorSpan(String.valueOf(mViewerCnt),
                getResources().getQuantityString(R.plurals.live_end_viewer_cnt, mViewerCnt, mViewerCnt),
                R.color.color_ffd439,
                R.color.color_white));
        if (mIsFailure) {
            //LIVEAND-2434 跟产品和张洋对过，房间5001时，不显示观看人数。
            mViewerTv.setVisibility(View.GONE);
        }

        mTicketTv = (TextView) mRootView.findViewById(R.id.ticket_tv);
        if (mIsShowTicketView) {
            mTicketTv.setText(SpanUtils.addColorSpan(String.valueOf(mTicket),
                    getResources().getQuantityString(R.plurals.live_end_ticket, mTicket, mTicket),
                    R.color.color_ffd439,
                    R.color.color_white));
        } else {
            mTicketTv.setVisibility(View.GONE);
        }

        mBackBtn = (TextView) mRootView.findViewById(R.id.back_btn);
        mBackBtn.setTag(VideoAction.ACTION_END_BACK);
        mBackBtn.setOnClickListener(this);

        mDeleteBtn = (TextView) mRootView.findViewById(R.id.delete_btn);

        //根据准备直播页面的CheckedTextView来确定是否显示删除按钮
        if (mIsAddHistory) {
            mDeleteBtn.setVisibility(View.VISIBLE);
            MyLog.d(TAG, " generateHistory = " + mGenerateHistorySucc);
            if (mGenerateHistorySucc) {
                mDeleteBtn.setTag(VideoAction.ACTION_END_HISTORY_DELETE);
                mDeleteBtn.setOnClickListener(this);
            } else {
                mDeleteBtn.setText(mGenerateHistoryMsg);
            }
        } else {
            mDeleteBtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int action = 0;
        try {
            if (v.getTag() != null) {
                action = Integer.valueOf(String.valueOf(v.getTag()));
            }
        } catch (NumberFormatException e) {
            MyLog.d(TAG, e);
            return;
        }

        switch (action) {
            case VideoAction.ACTION_END_BACK:
                getActivity().finish();
                String uri = "walilive://channel/channel_id=0";
                WaliliveProcessor.process(Uri.parse(uri), null, (RxActivity) getActivity(), false);
                break;
            case VideoAction.ACTION_END_HISTORY_DELETE:
                DialogUtils.showNormalDialog(getActivity(), 0, R.string.confirm_delete_replay, R.string.ok, R.string.cancel, new DialogUtils.IDialogCallback() {
                    @Override
                    public void process(DialogInterface dialogInterface, int i) {
//                        ThreadPool.runOnWorker(LiveTask.historyDelete(mRoomId, new WeakReference<>(this)));
                        getActivity().finish();
                    }
                }, null);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        mCurrentScrrenRotateIsLandScape = 0;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandlerThread.destroy();
    }

    @Override
    public boolean onBackPressed() {
        getActivity().finish();
        return true;
    }

    @Override
    public void processAction(String action, int errCode, Object... objects) {
        MyLog.d(TAG, "processAction : " + action + " , errCode : " + errCode);
        switch (action) {
            case MiLinkCommand.COMMAND_LIVE_HISTORY_DELETE:
                // nothing to do
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        KeyboardUtils.hideKeyboard(getActivity());
    }

    private void finish() {
        FragmentNaviUtils.popFragmentFromStack(getActivity());
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return null;
    }

    public static Bundle getBundle(long ownerId, String roomId, long avatarTs, int viewerCnt, int liveType, int ticket, String shareUrl,
                                   String location, User owner, String liveCoverUrl, String liveTitle) {
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_OWNER_ID, ownerId);
        bundle.putString(EXTRA_ROOM_ID, roomId);
        bundle.putString(EXTRA_LOCATION, location);
        bundle.putLong(EXTRA_AVATAR_TS, avatarTs);
        bundle.putInt(EXTRA_VIEWER_CNT, viewerCnt);
        bundle.putInt(EXTRA_LIVE_TYPE, liveType);
        bundle.putInt(EXTRA_TICKET, ticket);
        bundle.putString(EXTRA_SHARE_URL, shareUrl);
        bundle.putString(EXTRA_LOCATION, location);
        bundle.putSerializable(EXTRA_OWNER, owner);
        bundle.putString(EXTRA_GENERATE_COVER_URL, liveCoverUrl);
        bundle.putString(EXTRA_GENERATE_LIVE_TITLE, liveTitle);
        bundle.putBoolean(BaseFragment.PARAM_FORCE_PORTRAIT, true);
        return bundle;
    }

    public static Bundle getBundle(long ownerId, String roomId, long avatarTs, int viewerCnt, int liveType, int ticket, String shareUrl,
                                   String location, User owner, String liveCoverUrl, String liveTitle, int from) {
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_OWNER_ID, ownerId);
        bundle.putString(EXTRA_ROOM_ID, roomId);
        bundle.putString(EXTRA_LOCATION, location);
        bundle.putLong(EXTRA_AVATAR_TS, avatarTs);
        bundle.putInt(EXTRA_VIEWER_CNT, viewerCnt);
        bundle.putInt(EXTRA_LIVE_TYPE, liveType);
        bundle.putInt(EXTRA_TICKET, ticket);
        bundle.putString(EXTRA_SHARE_URL, shareUrl);
        bundle.putString(EXTRA_LOCATION, location);
        bundle.putSerializable(EXTRA_OWNER, owner);
        bundle.putString(EXTRA_GENERATE_COVER_URL, liveCoverUrl);
        bundle.putString(EXTRA_GENERATE_LIVE_TITLE, liveTitle);
        bundle.putInt(EXTRA_FROM, from);
        bundle.putBoolean(BaseFragment.PARAM_FORCE_PORTRAIT, true);
        return bundle;
    }

    public static void closeFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentNaviUtils.removeFragment(fragment);
        }
    }

    public static EndLiveFragment openFragment(BaseSdkActivity activity, Bundle bundle) {
        if (activity == null || activity.isFinishing()) {
            MyLog.d(TAG, "openFragment activity state is illegal");
            return null;
        }
        return (EndLiveFragment) FragmentNaviUtils.addFragment(activity, R.id.main_act_container, EndLiveFragment.class, bundle, true, false, true);
    }

    public static EndLiveFragment openFragment(BaseSdkActivity activity, int layoutId, long ownerId, String roomId, long avatarTs, int viewerCnt,
                                               int liveType, int ticket, String shareUrl, String location, User owner, String liveCoverUrl, String liveTitle) {
        if (activity == null || activity.isFinishing()) {
            MyLog.d(TAG, "openFragment activity state is illegal");
            return null;
        }
        Bundle bundle = getBundle(ownerId, roomId, avatarTs, viewerCnt, liveType, ticket, shareUrl, location, owner, liveCoverUrl, liveTitle);
        return (EndLiveFragment) FragmentNaviUtils.addFragment(activity, layoutId, EndLiveFragment.class, bundle, true, false, true);
    }

    public static EndLiveFragment openFragment(BaseSdkActivity activity, int layoutId, long ownerId, String roomId, long avatarTs, int viewerCnt,
                                               int liveType, int ticket, String shareUrl, String location, User owner, String liveCoverUrl, String liveTitle, int from) {
        if (activity == null || activity.isFinishing()) {
            MyLog.d(TAG, "openFragment activity state is illegal");
            return null;
        }
        Bundle bundle = getBundle(ownerId, roomId, avatarTs, viewerCnt, liveType, ticket, shareUrl, location, owner, liveCoverUrl, liveTitle, from);
        return (EndLiveFragment) FragmentNaviUtils.addFragment(activity, layoutId, EndLiveFragment.class, bundle, true, false, true);
    }

    public static EndLiveFragment openFragmentWithFailure(BaseSdkActivity activity, int layoutId, long ownerId, String roomId, long avatarTs, int viewerCnt, int liveType, int ticket,
                                                          String shareUrl, String location, User owner, String liveCoverUrl, String liveTitle) {
        if (activity == null || activity.isFinishing()) {
            MyLog.d(TAG, "openFragmentWithFailure activity state is illegal");
            return null;
        }
        MyLog.w(TAG, "ownerId" + ownerId + " roomid =" + roomId + " avatarTs=" + avatarTs + "viewerCnt=" + viewerCnt + "liveType=" + liveType + "shareUrl =" + shareUrl);
        Bundle bundle = getBundle(ownerId, roomId, avatarTs, viewerCnt, liveType, ticket, shareUrl, location, owner, liveCoverUrl, liveTitle);
        bundle.putBoolean(EXTRA_FAILURE, true);
        return (EndLiveFragment) FragmentNaviUtils.addFragment(activity, layoutId, EndLiveFragment.class, bundle, true, false, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyLog.d(TAG, "onActivityResult " + requestCode + " , resultCode=" + resultCode + " , data =" + data);
    }
}
