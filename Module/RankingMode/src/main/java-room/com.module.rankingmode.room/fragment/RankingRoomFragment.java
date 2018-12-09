package com.module.rankingmode.room.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.common.base.BaseFragment;
import com.common.emoji.EmotionKeyboard;
import com.common.emoji.EmotionLayout;
import com.common.emoji.IEmotionExtClickListener;
import com.common.emoji.IEmotionSelectedListener;
import com.common.emoji.LQREmotionKit;
import com.common.utils.U;
import com.common.view.ex.NoLeakEditText;
import com.module.rankingmode.R;

public class RankingRoomFragment extends BaseFragment {

    EmotionKeyboard mEmotionKeyboard;

    LinearLayout mBottomContainer;
    NoLeakEditText mEtContent;
    ImageView mIvEmo;
    EmotionLayout mElEmotion;
    ViewGroup mPlaceHolderView;

    @Override
    public int initView() {
        return R.layout.ranking_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        initInputView();
    }

    /**
     * 输入面板相关view的初始化
     */
    private void initInputView() {
        LQREmotionKit.tryInit(U.app());
        mBottomContainer = (LinearLayout) mRootView.findViewById(R.id.bottom_container);
        mEtContent = (NoLeakEditText) mRootView.findViewById(R.id.etContent);
        mIvEmo = (ImageView) mRootView.findViewById(R.id.ivEmo);
        mElEmotion = (EmotionLayout) mRootView.findViewById(R.id.elEmotion);
        mPlaceHolderView = mRootView.findViewById(R.id.place_holder_view);

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
    }

    private void initEmotionKeyboard() {
        mEmotionKeyboard = EmotionKeyboard.with(getActivity());
        mEmotionKeyboard.bindToPlaceHodlerView(mPlaceHolderView);
        mEmotionKeyboard.bindToEmotionButton(mIvEmo);
        mEmotionKeyboard.bindToEditText(mEtContent);
        mEmotionKeyboard.setEmotionLayout(mElEmotion);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }


    @Override
    protected boolean onBackPressed() {
        if (mEmotionKeyboard.isEmotionShown()) {
            mEmotionKeyboard.hideEmotion();
            return true;
        }
        return super.onBackPressed();
    }
}
