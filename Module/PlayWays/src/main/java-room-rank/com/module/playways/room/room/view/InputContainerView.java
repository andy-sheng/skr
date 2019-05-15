package com.module.playways.room.room.view;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.emoji.EmotionKeyboard;
import com.common.emoji.EmotionLayout;
import com.common.emoji.IEmotionExtClickListener;
import com.common.emoji.IEmotionSelectedListener;
import com.common.emoji.LQREmotionKit;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.NoLeakEditText;
import com.component.busilib.constans.GameModeType;
import com.module.playways.voice.activity.VoiceRoomActivity;
import com.module.playways.R;
import com.module.playways.room.msg.event.EventHelper;
import com.module.playways.room.room.RankRoomServerApi;
import com.module.playways.room.room.event.InputBoardEvent;
import com.module.playways.BaseRoomData;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class InputContainerView extends RelativeLayout implements EmotionKeyboard.BoardStatusListener {
    EmotionKeyboard mEmotionKeyboard;
    LinearLayout mInputContainer;
    protected NoLeakEditText mEtContent;
    ImageView mIvEmo;
    EmotionLayout mElEmotion;
    ViewGroup mPlaceHolderView;
    protected View mSendMsgBtn;

    protected boolean mHasPretend = false;

    protected Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                EventHelper.pretendCommentPush((String) msg.obj, mRoomData.getGameId());
                mHasPretend = true;
            }
        }
    };
    protected BaseRoomData mRoomData;

    public InputContainerView(Context context) {
        super(context);
        init();
    }

    public InputContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        inflate(getContext(), R.layout.input_container_view_layout, this);
        initInputView();
    }

    /**
     * 输入面板相关view的初始化
     */
    protected void initInputView() {

        LQREmotionKit.tryInit(U.app());
        mInputContainer = (LinearLayout) this.findViewById(R.id.et_container);
        mEtContent = (NoLeakEditText) this.findViewById(R.id.etContent);
        mIvEmo = (ImageView) this.findViewById(R.id.ivEmo);
        mElEmotion = (EmotionLayout) this.findViewById(R.id.elEmotion);
        mPlaceHolderView = this.findViewById(R.id.place_holder_view);
        mSendMsgBtn = this.findViewById(R.id.send_msg_btn);

        /**
         * 点击小表情自动添加到该 mEtContent 中
         */
        mElEmotion.attachEditText(mEtContent);

        mElEmotion.setEmotionAddVisiable(true);
        mElEmotion.setEmotionSettingVisiable(true);
        mElEmotion.setShowSticker(true);

        mElEmotion.setEmotionExtClickListener(new IEmotionExtClickListener() {
            @Override
            public void onEmotionAddClick(View view) {
                U.getToastUtil().showShort("add");
            }

            @Override
            public void onEmotionSettingClick(View view) {
                U.getToastUtil().showShort("setting");
            }
        });

        mElEmotion.setEmotionSelectedListener(new IEmotionSelectedListener() {
            @Override
            public void onEmojiSelected(String key) {

            }

            @Override
            public void onStickerSelected(String categoryName, String stickerName, String stickerBitmapPath) {
                String stickerPath = stickerBitmapPath;
                U.getToastUtil().showShort("stickerPath:" + stickerPath);
            }
        });

        initEmotionKeyboard();

        mSendMsgBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mHasPretend = false;
                if (mRoomData.getGameType() == GameModeType.GAME_MODE_CLASSIC_RANK) {
                    if (getContext() instanceof VoiceRoomActivity) {
                        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK), "chatroom_chat", null);
                    } else {
                        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK), "game_chat", null);
                    }
                } else if (mRoomData.getGameType() == GameModeType.GAME_MODE_GRAB) {
                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB), "game_chat", null);
                }
                String content = mEtContent.getText().toString();
                RankRoomServerApi roomServerApi = ApiManager.getInstance().createService(RankRoomServerApi.class);

                HashMap<String, Object> map = new HashMap<>();
                map.put("gameID", mRoomData.getGameId());
                map.put("content", content);

                RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
                ApiMethods.subscribe(roomServerApi.sendMsg(body), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult result) {
                        if (result.getErrno() == 0) {
                            mEtContent.setText("");
                            String content = result.getData().getString("afterFilterContent");
                            if (!mHasPretend && !TextUtils.isEmpty(content)) {
                                mUiHandler.removeMessages(100);
                                EventHelper.pretendCommentPush(content, mRoomData.getGameId());
                            }
                        }
                    }
                });
                Message msg = mUiHandler.obtainMessage();
                msg.what = 100;
                msg.obj = content;
                mUiHandler.removeMessages(100);
                mUiHandler.sendMessageDelayed(msg, 500);
                U.getKeyBoardUtils().hideSoftInputKeyBoard(U.getActivityUtils().getCurrentActivity());
            }
        });
    }

    private void initEmotionKeyboard() {
        mEmotionKeyboard = EmotionKeyboard.with((Activity) getContext());
        mEmotionKeyboard.bindToPlaceHodlerView(mPlaceHolderView);
        mEmotionKeyboard.bindToEmotionButton(mIvEmo);
        mEmotionKeyboard.bindToEditText(mEtContent);
        mEmotionKeyboard.setEmotionLayout(mElEmotion);

        mEmotionKeyboard.setBoardStatusListener(this);
    }

    @Override
    public void onBoradShow() {
        EventBus.getDefault().post(new InputBoardEvent(true));
        mInputContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBoradHide() {
        EventBus.getDefault().post(new InputBoardEvent(false));
        mInputContainer.setVisibility(View.GONE);
    }

    public void showSoftInput() {
        mEmotionKeyboard.showSoftInput();
    }

    public void hideSoftInput() {
        mEmotionKeyboard.hideSoftInput();
    }

    public boolean onBackPressed() {
        if (mEmotionKeyboard.isEmotionShown()) {
            mEmotionKeyboard.hideEmotionLayout(false);
            return true;
        }
        return false;
    }

    public void setRoomData(BaseRoomData roomData) {
        mRoomData = roomData;
//        EventHelper.pretendCommentPush("请注意保持直播间和谐", mRoomData.getGameId());
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mEmotionKeyboard.destroy();
        mUiHandler.removeCallbacksAndMessages(null);
    }
}
