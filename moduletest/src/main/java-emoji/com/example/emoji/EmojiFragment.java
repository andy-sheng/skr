package com.example.emoji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.common.base.BaseFragment;
import com.common.emoji.EmotionKeyboard;
import com.common.emoji.EmotionLayout;
import com.common.emoji.IEmotionExtClickListener;
import com.common.emoji.IEmotionSelectedListener;
import com.common.emoji.LQREmotionKit;
import com.common.utils.U;
import com.wali.live.moduletest.R;

public class EmojiFragment extends BaseFragment {

    LinearLayout mLlContent;
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
        LQREmotionKit.init(U.app());
        mLlContent = (LinearLayout) mRootView.findViewById(R.id.llContent);
        mEtContent = (EditText) mRootView.findViewById(R.id.etContent);
        mIvEmo = (ImageView) mRootView.findViewById(R.id.ivEmo);
        mElEmotion = (EmotionLayout) mRootView.findViewById(R.id.elEmotion);

        initEmotionKeyboard();

        mElEmotion.attachEditText(mEtContent);
        mElEmotion.setEmotionAddVisiable(true);
        mElEmotion.setEmotionSettingVisiable(true);
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
        return false;
    }
}
