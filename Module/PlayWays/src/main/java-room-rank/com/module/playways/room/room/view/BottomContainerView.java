package com.module.playways.room.room.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.CustomMsgType;
import com.module.msg.IMsgService;
import com.module.playways.room.msg.BasePushInfo;
import com.module.playways.room.msg.event.SpecialEmojiMsgEvent;
import com.module.playways.RoomDataUtils;
import com.module.rank.R;
import com.module.playways.room.room.event.InputBoardEvent;
import com.module.playways.BaseRoomData;
import com.module.playways.room.room.quickmsg.QuickMsgView;
import com.zq.live.proto.Common.ESex;
import com.zq.live.proto.Common.UserInfo;
import com.zq.live.proto.Room.EMsgPosType;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.RoomMsg;
import com.zq.live.proto.Room.SpecialEmojiMsg;
import com.zq.live.proto.Room.SpecialEmojiMsgType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

/**
 * 一唱到底的看这个
 * {@link com.module.playways.grab.room.bottom.GrabBottomContainerView}
 * 排位赛看这个
 * {@link com.module.playways.room.room.bottom.RankBottomContainerView}
 */
public abstract class BottomContainerView extends RelativeLayout {

    static final int CLEAR_CONTINUE_FLAG = 11;

    protected BaseRoomData mRoomData;

    protected Listener mBottomContainerListener;

    protected View mQuickBtn;

    protected ExTextView mShowInputContainerBtn;
    protected ExImageView mEmoji2Btn;
    protected ExImageView mEmoji1Btn;


    protected PopupWindow mQuickMsgPopWindow;  //快捷词弹出面板

    protected SpecialEmojiMsgType mLastSendType = null;
    protected int mContinueCount = 1;
    protected long mContinueId = 0L;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == CLEAR_CONTINUE_FLAG) {
                mLastSendType = null;
                mContinueCount = 1;
            }
        }
    };

    public BottomContainerView(Context context) {
        super(context);
        init();
    }

    public BottomContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected abstract int getLayout();

    protected void onQuickMsgDialogShow(boolean show) {

    }

    protected void init() {
        inflate(getContext(), getLayout(), this);

        mQuickBtn = this.findViewById(R.id.quick_btn);
        mShowInputContainerBtn = this.findViewById(R.id.show_input_container_btn);
        mEmoji2Btn = this.findViewById(R.id.emoji2_btn);
        mEmoji1Btn = this.findViewById(R.id.emoji1_btn);

        mShowInputContainerBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mBottomContainerListener != null) {
                    mBottomContainerListener.showInputBtnClick();
                }
            }
        });

        mQuickBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                int w = U.getDisplayUtils().getScreenWidth() - U.getDisplayUtils().dip2px(32);
                int h = U.getDisplayUtils().dip2px(172);
                if (mQuickMsgPopWindow == null) {
                    QuickMsgView quickMsgView = new QuickMsgView(getContext());
                    quickMsgView.setRoomData(mRoomData);
                    quickMsgView.setListener(new QuickMsgView.Listener() {
                        @Override
                        public void onSendMsgOver() {
                            if (mQuickMsgPopWindow != null) {
                                mQuickMsgPopWindow.dismiss();
                            }
                        }
                    });
                    mQuickMsgPopWindow = new PopupWindow(quickMsgView, w, h);
//                    mQuickMsgPopWindow.setFocusable(false);
                    // 去除动画
                    mQuickMsgPopWindow.setAnimationStyle(R.style.MyPopupWindow_anim_style);
                    mQuickMsgPopWindow.setBackgroundDrawable(new BitmapDrawable());
                    mQuickMsgPopWindow.setOutsideTouchable(true);
                    mQuickMsgPopWindow.setFocusable(true);
                    mQuickMsgPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            onQuickMsgDialogShow(false);
                        }
                    });
                }
                if (!mQuickMsgPopWindow.isShowing()) {
                    int l[] = new int[2];
                    mQuickBtn.getLocationInWindow(l);
                    mQuickMsgPopWindow.showAtLocation(mQuickBtn, Gravity.START | Gravity.TOP, l[0], l[1] - h - U.getDisplayUtils().dip2px(5));
                    onQuickMsgDialogShow(true);
                }else {
                    mQuickMsgPopWindow.dismiss();
                }
            }
        });
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        mEmoji1Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 发送动态表情，粑粑
                sendSpecialEmojiMsg(SpecialEmojiMsgType.SP_EMOJI_TYPE_UNLIKE, "扔了粑粑");
                HashMap map = new HashMap();
                map.put("expressionId2", String.valueOf(SpecialEmojiMsgType.SP_EMOJI_TYPE_UNLIKE.getValue()));
                if (mRoomData.getGameType() == GameModeType.GAME_MODE_CLASSIC_RANK) {
                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK), "game_expression", map);
                } else if (mRoomData.getGameType() == GameModeType.GAME_MODE_GRAB) {
                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB), "game_expression", map);
                }
            }
        });

        mEmoji2Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 发送动态表情，爱心
                sendSpecialEmojiMsg(SpecialEmojiMsgType.SP_EMOJI_TYPE_LIKE, "送出爱心");
                HashMap map = new HashMap();
                map.put("expressionId2", String.valueOf(SpecialEmojiMsgType.SP_EMOJI_TYPE_LIKE.getValue()));
                if (mRoomData.getGameType() == GameModeType.GAME_MODE_CLASSIC_RANK) {
                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK), "game_expression", map);
                } else if (mRoomData.getGameType() == GameModeType.GAME_MODE_GRAB) {
                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB), "game_expression", map);
                }
            }
        });
    }

    void sendSpecialEmojiMsg(SpecialEmojiMsgType type, String actionDesc) {
        if (RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
            U.getToastUtil().showShort("暂时不能给自己送礼哦");
            return;
        }
        IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
        if (msgService != null) {
            long ts = System.currentTimeMillis();
            int count;
            if (type == mLastSendType) {
                count = mContinueCount + 1;
            } else {
                count = 1;
                mContinueId = System.currentTimeMillis();
            }

            UserInfo senderInfo = new UserInfo.Builder()
                    .setUserID((int) MyUserInfoManager.getInstance().getUid())
                    .setNickName(MyUserInfoManager.getInstance().getNickName())
                    .setAvatar(MyUserInfoManager.getInstance().getAvatar())
                    .setSex(ESex.fromValue(MyUserInfoManager.getInstance().getSex()))
                    .setDescription("")
                    .setIsSystem(false)
                    .build();

            RoomMsg roomMsg = new RoomMsg.Builder()
                    .setTimeMs(ts)
                    .setMsgType(ERoomMsgType.RM_SPECIAL_EMOJI)
                    .setRoomID(mRoomData.getGameId())
                    .setNo(ts)
                    .setPosType(EMsgPosType.EPT_UNKNOWN)
                    .setSender(senderInfo)
                    .setSpecialEmojiMsg(new SpecialEmojiMsg.Builder()
                            .setContinueId(mContinueId)
                            .setEmojiType(type)
                            .setCount(count)
                            .setEmojiAction(actionDesc)
                            .build()
                    )
                    .build();

            String contnet = U.getBase64Utils().encode(roomMsg.toByteArray());
            msgService.sendChatRoomMessage(String.valueOf(mRoomData.getGameId()), CustomMsgType.MSG_TYPE_ROOM, contnet, new ICallback() {
                @Override
                public void onSucess(Object obj) {
                    mContinueCount = count;
                    mLastSendType = type;
                    mHandler.removeMessages(CLEAR_CONTINUE_FLAG);
                    // 5秒后连送重置
                    mHandler.sendEmptyMessageDelayed(CLEAR_CONTINUE_FLAG, 5000);

                    /**
                     * 伪装成push过去
                     */
                    BasePushInfo basePushInfo = new BasePushInfo();
                    basePushInfo.setRoomID(mRoomData.getGameId());
                    basePushInfo.setSender(senderInfo);
                    basePushInfo.setTimeMs(ts);
                    basePushInfo.setNo(ts);

                    SpecialEmojiMsgEvent specialEmojiMsgEvent = new SpecialEmojiMsgEvent(basePushInfo);
                    specialEmojiMsgEvent.emojiType = type;
                    specialEmojiMsgEvent.count = count;
                    specialEmojiMsgEvent.action = actionDesc;
                    specialEmojiMsgEvent.coutinueId = mContinueId;

                    EventBus.getDefault().post(specialEmojiMsgEvent);
                }

                @Override
                public void onFailed(Object obj, int errcode, String message) {
                    //TODO test 测试需要
                    onSucess(obj);
                }
            });
        }
    }

    public void dismissPopWindow() {
        if (mQuickMsgPopWindow != null) {
            mQuickMsgPopWindow.dismiss();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MyLog.d("BottomContainerView", "onDetachedFromWindow");
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        dismissPopWindow();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(InputBoardEvent event) {
        if (event.show) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    }

    public void setListener(Listener l) {
        mBottomContainerListener = l;
    }

    public void setRoomData(BaseRoomData roomData) {
        mRoomData = roomData;
    }

    public static abstract class Listener {
        public abstract void showInputBtnClick();

        public void clickRoomManagerBtn() {
        }
    }

}
