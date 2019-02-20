package com.module.playways.rank.room.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.CustomMsgType;
import com.module.msg.IMsgService;
import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.msg.event.SpecialEmojiMsgEvent;
import com.module.playways.RoomDataUtils;
import com.module.rank.R;
import com.module.playways.rank.room.event.InputBoardEvent;
import com.module.playways.RoomData;
import com.module.playways.rank.room.quickmsg.QuickMsgView;
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

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

public class BottomContainerView extends RelativeLayout {

    static final int CLEAR_CONTINUE_FLAG = 11;

    Listener mBottomContainerListener;

    ExImageView mQuickBtn;
    ExImageView mShowInputContainerBtn;
    ExImageView mEmoji2Btn;
    ExImageView mEmoji1Btn;

    PopupWindow mQuickMsgPopWindow;
    private RoomData mRoomData;

    SpecialEmojiMsgType mLastSendType = null;
    int mContinueCount = 1;
    long mContinueId = 0L;

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

    private void init() {
        inflate(getContext(), R.layout.bottom_container_view_layout, this);

        mQuickBtn = (ExImageView) this.findViewById(R.id.quick_btn);
        mShowInputContainerBtn = (ExImageView) this.findViewById(R.id.show_input_container_btn);
        mEmoji2Btn = (ExImageView) this.findViewById(R.id.emoji2_btn);
        mEmoji1Btn = (ExImageView) this.findViewById(R.id.emoji1_btn);

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
                    mQuickMsgPopWindow.setFocusable(false);
                    // 去除动画
//                    mQuickMsgPopWindow.setAnimationStyle(R.style.anim_quickmsg_dialog);
                    mQuickMsgPopWindow.setBackgroundDrawable(new BitmapDrawable());
                    mQuickMsgPopWindow.setOutsideTouchable(true);
                }
                if (!mQuickMsgPopWindow.isShowing()) {
                    int l[] = new int[2];
                    mQuickBtn.getLocationInWindow(l);
                    mQuickMsgPopWindow.showAtLocation(mQuickBtn, Gravity.START | Gravity.TOP, l[0], l[1] - h - U.getDisplayUtils().dip2px(5));
                }
            }
        });
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        mEmoji1Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 发送动态表情，爱心
                sendSpecialEmojiMsg(SpecialEmojiMsgType.SP_EMOJI_TYPE_UNLIKE, "扔了粑粑");
            }
        });
        mEmoji2Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 发送动态表情，爱心
                sendSpecialEmojiMsg(SpecialEmojiMsgType.SP_EMOJI_TYPE_LIKE, "送出爱心");
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

    public void setRoomData(RoomData roomData) {
        mRoomData = roomData;
    }

    public interface Listener {
        void showInputBtnClick();
    }

}
