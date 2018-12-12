package com.module.rankingmode.room.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.emoji.EmotionKeyboard;
import com.common.emoji.EmotionLayout;
import com.common.emoji.IEmotionExtClickListener;
import com.common.emoji.IEmotionSelectedListener;
import com.common.emoji.LQREmotionKit;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.ex.NoLeakEditText;
import com.module.rankingmode.R;
import com.module.rankingmode.msg.event.EventHelper;
import com.module.rankingmode.room.RoomServerApi;
import com.module.rankingmode.room.event.InputBoardEvent;
import com.module.rankingmode.room.fragment.RankingRoomFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class InputContainerView extends RelativeLayout {
    EmotionKeyboard mEmotionKeyboard;
    LinearLayout mInputContainer;
    NoLeakEditText mEtContent;
    ImageView mIvEmo;
    EmotionLayout mElEmotion;
    ViewGroup mPlaceHolderView;
    View mSendMsgBtn;

    public InputContainerView(Context context) {
        super(context);
        init();
    }

    public InputContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(),R.layout.input_container_view_layout,this);
        initInputView();
    }

    /**
     * 输入面板相关view的初始化
     */
    private void initInputView() {

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

        mSendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEtContent.getText().toString();
                RoomServerApi roomServerApi = ApiManager.getInstance().createService(RoomServerApi.class);

                HashMap<String, Object> map = new HashMap<>();
                map.put("gameID", 111);
                map.put("content", content);

                RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
                ApiMethods.subscribe(roomServerApi.sendMsg(body), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult obj) {

                    }
                });

                EventHelper.pretendCommentPush(content);
            }
        });
    }

    private void initEmotionKeyboard() {
        mEmotionKeyboard = EmotionKeyboard.with((Activity) getContext());
        mEmotionKeyboard.bindToPlaceHodlerView(mPlaceHolderView);
        mEmotionKeyboard.bindToEmotionButton(mIvEmo);
        mEmotionKeyboard.bindToEditText(mEtContent);
        mEmotionKeyboard.setEmotionLayout(mElEmotion);

        mEmotionKeyboard.setBoardStatusListener(new EmotionKeyboard.BoardStatusListener() {
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
        });
    }


    public void showSoftInput() {
        mEmotionKeyboard.showSoftInput();
    }

    public boolean onBackPressed() {
        if (mEmotionKeyboard.isEmotionShown()) {
            mEmotionKeyboard.hideEmotionLayout(false);
            return true;
        }
        return false;
    }
}
