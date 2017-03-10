package com.wali.live.watchsdk.endlive;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.fragment.BaseEventBusFragment;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.image.fresco.BaseImageView;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.language.LocaleUtil;
import com.base.utils.toast.ToastUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.live.module.common.R;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.proto.RoomRecommend;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.auth.AccountAuthManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * @module 新版直播结束页面 （用户界面）
 * Created by jiyangli on 16-7-4.
 */
public class UserEndLiveFragment extends BaseEventBusFragment implements View.OnClickListener, IUserEndLiveView {
    private static final String TAG = UserEndLiveFragment.class.getSimpleName();
    public static final String EXTRA_OWNER_ID = "extra_owner_id";
    public static final String EXTRA_ROOM_ID = "extra_room_id";
    public static final String EXTRA_AVATAR_TS = "extra_avatar_ts";
    public static final String EXTRA_FROM = "extra_from";
    public static final String EXTRA_OWNER = "extra_owner";
    public static final String EXTRA_VIEWER = "extra_viewer";
    public static final String EXTRA_LIVE_TYPE = "extra_live_type";

    private int mLiveType = LiveManager.TYPE_LIVE_PUBLIC;
    private long mUuId;
    private String mRoomId;

    private View mTouchDelegateView;
    //主播背景图
    private SimpleDraweeView mAvatarBg;
    //主播头像
    private BaseImageView imgAvatar;
    //主播名称
    private TextView txtAvatarName;
    //关注按钮
    private TextView txtFollow;
    //关注按钮
    private TextView txtViewer;
    //进入聊天室按钮
    private LinearLayout llytChatRoom;
    //前往首页
    private TextView imgHomePage;

    private ImageView imgClose;

    //------底部主播推荐相关控件------
    private RelativeLayout rlytHint;
    private RelativeLayout rlytRecommend;
    private RecyclerView rylvRecommend;

    private EndLivePresenter presenter;


    private FrameLayout flytFirstRoot;
    private FrameLayout flytSecondRoot;
    private FrameLayout flytThirdRoot;
    private FrameLayout flytFourthRoot;

    private BaseImageView imgFirst;
    private BaseImageView imgSecond;
    private BaseImageView imgThird;
    private BaseImageView imgFourth;

    private int count = 0;

    private void initFromArguments() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mLiveType = arguments.getInt(EXTRA_LIVE_TYPE);
            mUuId = arguments.getLong(EXTRA_OWNER_ID);
            mRoomId = arguments.getString(EXTRA_ROOM_ID);
        }
    }

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.end_live_layout_new, container, false);
    }

    @Override
    protected void bindView() {
        KeyboardUtils.hideKeyboard(getActivity());
        initView();
        initFromArguments();
        initData();
    }

    public void setTouchDelegate(View v) {
        this.mTouchDelegateView = v;
    }

    public void setPresenter(EndLivePresenter endLivePresenter) {
        presenter = endLivePresenter;
    }

    private void initView() {
        mAvatarBg = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_bg_dv);
        imgAvatar = (BaseImageView) mRootView.findViewById(R.id.end_live_ImgAvatar);
        txtAvatarName = (TextView) mRootView.findViewById(R.id.end_live_txtAvatarName);
        txtFollow = (TextView) mRootView.findViewById(R.id.end_live_txtFollow);
        llytChatRoom = (LinearLayout) mRootView.findViewById(R.id.end_live_llytChatRoom);
        rlytHint = (RelativeLayout) mRootView.findViewById(R.id.end_live_bottom_rlytHint);
        rylvRecommend = (RecyclerView) mRootView.findViewById(R.id.end_live_rylvRecommend);
        imgHomePage = (TextView) mRootView.findViewById(R.id.end_live_txtHomePage);
        txtViewer = (TextView) mRootView.findViewById(R.id.end_live_txtViewer);
        imgClose = (ImageView) mRootView.findViewById(R.id.end_live_btnClose);
        rlytRecommend = (RelativeLayout) mRootView.findViewById(R.id.end_live_rlytRecommend);

        flytFirstRoot = (FrameLayout) mRootView.findViewById(R.id.end_live_flytFirstRoot);
        flytSecondRoot = (FrameLayout) mRootView.findViewById(R.id.end_live_flytSecondRoot);
        flytThirdRoot = (FrameLayout) mRootView.findViewById(R.id.end_live_flytThirdRoot);
        flytFourthRoot = (FrameLayout) mRootView.findViewById(R.id.end_live_flytFourthRoot);
        imgFirst = (BaseImageView) mRootView.findViewById(R.id.end_live_imgFirst);
        imgSecond = (BaseImageView) mRootView.findViewById(R.id.end_live_imgSecond);
        imgThird = (BaseImageView) mRootView.findViewById(R.id.end_live_imgThird);
        imgFourth = (BaseImageView) mRootView.findViewById(R.id.end_live_imgFourth);

        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mTouchDelegateView != null) {
                    mTouchDelegateView.onTouchEvent(event);
                }
                return true;
            }
        });
    }

    /**
     * 关注与取消关注
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FollowOrUnfollowEvent event) {
        if (event == null) {
            MyLog.v(TAG, " onEventMainThread FollowOrUnfollowEvent event is null");
            return;
        }

        int type = event.eventType;
        long uuid = event.uuid;
        if (presenter.getOwnerId() == uuid) {
            updateFollowTv(type != FollowOrUnfollowEvent.EVENT_TYPE_UNFOLLOW);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        if (event == null) {
            return;
        }
        MyLog.w(TAG, "RoomDataChangeEvent " + event.type);
        switch (event.type) {
            case RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE: {
                RoomBaseDataModel roomBaseDataModel = event.source;
                if (roomBaseDataModel != null) {
                    String nickName = roomBaseDataModel.getNickName();
                    if (!TextUtils.isEmpty(nickName)) {
                        txtAvatarName.setText(nickName);
                    }
                    updateFollowTv(roomBaseDataModel.isFocused());
                    if (txtFollow.getVisibility() != View.VISIBLE) {
                        txtFollow.setVisibility(View.VISIBLE);
                    }
                }
            }
            break;
            default:
                break;
        }
    }

    private void updateFollowTv(boolean isFocused) {
        MyLog.w(TAG, "isFocused=" + isFocused);
        if (isFocused) {
            txtFollow.setText(getResources().getString(R.string.live_ended_concerned));
            txtFollow.setTextColor(getResources().getColor(R.color.color_white_trans_40));
            txtFollow.setBackgroundResource(R.drawable.followed_button_normal);
        } else {
            txtFollow.setText(getResources().getString(R.string.add_follow));
            txtFollow.setBackgroundResource(R.drawable.chat_room_button);
            txtFollow.setTextColor(getResources().getColor(R.color.color_black_trans_90));
            txtFollow.setOnClickListener(this);
        }
    }

    @Override
    public void initData() {
        AvatarUtils.loadAvatarByUidTs(mAvatarBg, presenter.getOwnerId(), presenter.getAvatarTs(), AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false, true);
        AvatarUtils.loadAvatarByUidTs(imgAvatar, presenter.getOwnerId(), 0, true);

        if (TextUtils.isEmpty(presenter.getNickName()) || presenter.isMyReplay()) {
            txtFollow.setVisibility(View.INVISIBLE);
        } else {
            txtAvatarName.setText(presenter.getNickName());
            txtFollow.setVisibility(View.VISIBLE);
        }
        if (presenter.getViewerCount() < 0) {
            txtViewer.setVisibility(View.INVISIBLE);
        } else {
            txtViewer.setText(getResources().getString(R.string.live_ended_viewer_cnt, String.valueOf(presenter.getViewerCount())));
        }
        if (presenter.isFocused()) {
            txtFollow.setText(getResources().getString(R.string.live_ended_concerned));
            txtFollow.setTextColor(getResources().getColor(R.color.color_white_trans_40));
            txtFollow.setBackgroundResource(R.drawable.followed_button_normal);
        } else {
            txtFollow.setText(getResources().getString(R.string.add_follow));
            txtFollow.setOnClickListener(this);
        }

        imgHomePage.setOnClickListener(this);
        imgAvatar.setOnClickListener(this);
        imgClose.setOnClickListener(this);

        if (LocaleUtil.getLanguageCode().equals("zh_CN") || LocaleUtil.getLanguageCode().equals("zh_TW")) {
            Observable.interval(0, 10, TimeUnit.SECONDS)
                    .compose(this.<Long>bindUntilEvent())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            MyLog.e(TAG, "getRoomRecommendList error");
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(Long aLong) {
                            presenter.getRoomRecommendList();
                        }
                    });
        } else {
            MyLog.d(TAG, "Language is not zh_CN");
            hideAvatarZone();
        }
    }

    @Override
    public boolean onBackPressed() {
        getActivity().finish();
        return true;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.end_live_btnClose) {
            presenter.sendCloseCommend();
            getActivity().finish();

        } else if (i == R.id.end_live_txtHomePage) {
//            String uri = "walilive://channel/channel_id=0";
            // 跳到主播主页
//            try {
//                Intent intent = null;
//                intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME);
//                SafeGoActivity.goCheckUpdateWhenFailed(getActivity(), intent);
//                popFragment();
//            } catch (URISyntaxException e) {
//            }
            // 打点
//            presenter.sendHomePageCommend();
        } else if (i == R.id.end_live_txtFollow) {
            if (AccountAuthManager.triggerActionNeedAccount(getActivity())) {
                followAvatar();
            }

        } else if (i == R.id.end_live_ImgAvatar) {
//            PersonInfoActivity.openActivity(getActivity(), presenter.getOwnerId(), presenter.getOwnerCertType());
//            presenter.sendClickAvatarCommend(presenter.getOwnerId());

        }
    }

    /**
     * 显示第一个推荐主播
     */
    @Override
    public void showFirstAvatar(final RoomRecommend.RecommendRoom roomData) {
        if (roomData == null) {
            return;
        }
        AvatarUtils.loadAvatarNoLoading(imgFirst, roomData.getZuid(), roomData.getAvatar(), AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false);
        imgFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startWatchActivity(getActivity(), roomData, 1);
                popFragment();
            }
        });

        imgFirst.postDelayed(new Runnable() {
            @Override
            public void run() {
                flytFirstRoot.setVisibility(View.VISIBLE);
            }
        }, 200);
    }

    /**
     * 显示第二个推荐主播
     */
    @Override
    public void showSecondAvatar(final RoomRecommend.RecommendRoom roomData) {
        if (roomData == null) {
            return;
        }
        AvatarUtils.loadAvatarNoLoading(imgSecond, roomData.getZuid(), roomData.getAvatar(), AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false);
        imgSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startWatchActivity(getActivity(), roomData, 2);
                popFragment();
            }
        });


        imgSecond.postDelayed(new Runnable() {
            @Override
            public void run() {
                flytSecondRoot.setVisibility(View.VISIBLE);
            }
        }, 200);
    }

    /**
     * 显示第三个推荐主播
     */
    @Override
    public void showThirdAvatar(final RoomRecommend.RecommendRoom roomData) {
        if (roomData == null) {
            return;
        }
        AvatarUtils.loadAvatarNoLoading(imgThird, roomData.getZuid(), roomData.getAvatar(), AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false);
        imgThird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startWatchActivity(getActivity(), roomData, 3);
                popFragment();
            }
        });


        imgThird.postDelayed(new Runnable() {
            @Override
            public void run() {
                flytThirdRoot.setVisibility(View.VISIBLE);
            }
        }, 200);
    }

    /**
     * 显示第四个推荐主播
     */
    @Override
    public void showFourthAvatar(final RoomRecommend.RecommendRoom roomData) {
        if (roomData == null) {
            return;
        }
        AvatarUtils.loadAvatarNoLoading(imgFourth, roomData.getZuid(), roomData.getAvatar(), AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false);
        imgFourth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startWatchActivity(getActivity(), roomData, 4);
                popFragment();
            }
        });

        imgFourth.postDelayed(new Runnable() {
            @Override
            public void run() {
                flytFourthRoot.setVisibility(View.VISIBLE);
            }
        }, 200);
    }


    private void followAvatar() {
        presenter.followAvatar();
    }

    /**
     * 隐藏底部主播推荐页面
     */
    @Override
    public void hideAvatarZone() {
        rlytRecommend.setVisibility(View.GONE);
        rlytHint.setVisibility(View.GONE);
        rylvRecommend.setVisibility(View.GONE);
    }

    /**
     * 更新关注按钮UI
     */
    @Override
    public void setFollowText() {
        txtFollow.setClickable(false);
        txtFollow.setText(R.string.live_ended_concerned);
        txtFollow.setTextColor(getResources().getColor(R.color.color_7a7a7a));
        txtFollow.setBackgroundResource(R.drawable.followed_button_normal);
    }

    List<RoomRecommend.RecommendRoom> roomDatas = new ArrayList<>();

    @Override
    public void popFragment() {
        Observable.timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((RxActivity) getContext())
                        .bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        FragmentNaviUtils.popFragment(getActivity());
                    }
                });
    }


    /**
     * 显示关注主播结果
     */
    @Override
    public void followResult(boolean result) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            if (result) {
                presenter.sendFollowCommend();
                ToastUtils.showToast(getActivity(), getString(R.string.endlive_follow_success));
                setFollowText();
            } else {
                txtFollow.setClickable(true);
                txtFollow.setText(R.string.live_ended_concern);
                ToastUtils.showToast(getActivity(), getString(R.string.follow_failed));
            }
        }
    }

    /**
     * 显示获取推荐主播
     *
     * @param result
     */
    @Override
    public void getRoomListResult(List<RoomRecommend.RecommendRoom> result) {

        if (result.size() > 0) {
            showFirstAvatar(result.get(0));
            showSecondAvatar(result.get(1));
            showThirdAvatar(result.get(2));
            showFourthAvatar(result.get(4));
            if (count == 0) {
                count++;
                presenter.sendShowCommend(result.get(0).getZuid(), result.get(1).getZuid(), result.get(2).getZuid(), result.get(3).getZuid());
            }
        } else {
            MyLog.d(TAG, "size less than 1 ");
            hideAvatarZone();
        }
    }

    /**
     * 打开直播结束页面
     *
     * @param activity Context>>>>
     * @param layoutId 用来显示结束页面的layout id
     * @param ownerId  主播ID
     * @param roomId   房间Id
     * @param avatarTs 时间戳
     * @param owner    主播
     * @param viewer   观众数
     * @param liveType 房间类型Live.proto里定义
     * @return
     */
    public static UserEndLiveFragment openFragment(FragmentActivity activity, int layoutId, long ownerId, String roomId, long avatarTs, User owner, int viewer, int liveType) {
        if (activity == null || activity.isFinishing()) {
            MyLog.d(TAG, "openFragment activity state is illegal");
            return null;
        }
        Bundle bundle = getBundle(ownerId, roomId, avatarTs, owner, viewer, liveType);
        return (UserEndLiveFragment) FragmentNaviUtils.addFragment(activity, layoutId, UserEndLiveFragment.class, bundle, true, false, true);
    }

    public static Bundle getBundle(long ownerId, String roomId, long avatarTs, User owner, int viewer, int liveType) {
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_OWNER_ID, ownerId);
        bundle.putString(EXTRA_ROOM_ID, roomId);
        bundle.putLong(EXTRA_AVATAR_TS, avatarTs);
        bundle.putSerializable(EXTRA_OWNER, owner);
        bundle.putBoolean(BaseFragment.PARAM_FORCE_PORTRAIT, true);
        bundle.putInt(EXTRA_VIEWER, viewer);
        bundle.putInt(EXTRA_LIVE_TYPE, liveType);
        return bundle;
    }

}
