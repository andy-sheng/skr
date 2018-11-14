package com.example.emoji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.emoji.EmotionKeyboard;
import com.common.emoji.EmotionLayout;
import com.common.emoji.IEmotionExtClickListener;
import com.common.emoji.IEmotionSelectedListener;
import com.common.emoji.LQREmotionKit;
import com.common.utils.KeyboardEvent;
import com.common.utils.U;
import com.wali.live.moduletest.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * 明天，只有一个viewpager，支持add按钮 设置按钮隐藏
 */
public class EmojiFragment extends BaseFragment {

    ViewGroup mLlContent;
    EditText mEtContent;
    ImageView mIvEmo;
    EmotionLayout mElEmotion;

    private EmotionKeyboard mEmotionKeyboard;


    @Override
    public int initView() {
        return R.layout.emoji_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        // 测试一下
        TextView changeBtn = mRootView.findViewById(R.id.change_btn);
        if(getActivity() instanceof EmojiActivity) {
            EmojiActivity emojiActivity = (EmojiActivity) getActivity();
            changeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emojiActivity.resizeLayoutSelfWhenKeybordShow = !emojiActivity.resizeLayoutSelfWhenKeybordShow;
                    if (emojiActivity.resizeLayoutSelfWhenKeybordShow) {
                        changeBtn.setText("自己控制");
                    } else {
                        changeBtn.setText("自动控制");
                    }
                }
            });
        }

        LQREmotionKit.init(U.app());
        mLlContent =  mRootView.findViewById(R.id.llContent);
        mEtContent =  mRootView.findViewById(R.id.etContent);
        mIvEmo =  mRootView.findViewById(R.id.ivEmo);
        mElEmotion =  mRootView.findViewById(R.id.elEmotion);

        initEmotionKeyboard();

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
                U.getToastUtil().showToast("add");
            }

            @Override
            public void onEmotionSettingClick(View view) {
                U.getToastUtil().showToast("setting");
            }
        });

        mElEmotion.setEmotionSelectedListener(new IEmotionSelectedListener() {
            @Override
            public void onEmojiSelected(String key) {

            }

            @Override
            public void onStickerSelected(String categoryName, String stickerName, String stickerBitmapPath) {
                String stickerPath = stickerBitmapPath;
                U.getToastUtil().showToast("stickerPath:" + stickerPath);
            }
        });

    }

    private void initEmotionKeyboard() {
        mEmotionKeyboard = EmotionKeyboard.with(getActivity());
        mEmotionKeyboard.bindToContent(mLlContent);
        mEmotionKeyboard.bindToEmotionButton(mIvEmo);
        mEmotionKeyboard.bindToEditText(mEtContent);
        mEmotionKeyboard.setEmotionLayout(mElEmotion);
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(KeyboardEvent event) {
        if (event.from.equals(getActivity().getClass().getName())) {
            U.getToastUtil().showToast(event.toString());
        }
    }
}
