package com.module.rankingmode.room.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.emoji.EmotionKeyboard;
import com.common.emoji.EmotionLayout;
import com.common.emoji.IEmotionExtClickListener;
import com.common.emoji.IEmotionSelectedListener;
import com.common.emoji.LQREmotionKit;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.module.rankingmode.R;

public class RankingRoomFragment extends BaseFragment {

    EmotionKeyboard mEmotionKeyboard;

    LinearLayout mInputContainer;
    NoLeakEditText mEtContent;
    ImageView mIvEmo;
    EmotionLayout mElEmotion;
    ViewGroup mPlaceHolderView;

    RelativeLayout mBottomContainer;
    ExImageView mEmoji1Btn;
    ExTextView mShowInputContainerBtn;
    ExImageView mEmoji4Btn;
    ExImageView mEmoji3Btn;
    ExImageView mEmoji2Btn;

    @Override
    public int initView() {
        return R.layout.ranking_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        initInputView();
        initBottomView();
    }

    /**
     * 输入面板相关view的初始化
     */
    private void initInputView() {
        LQREmotionKit.tryInit(U.app());
        mInputContainer = (LinearLayout) mRootView.findViewById(R.id.et_container);
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

        mEmotionKeyboard.setBoardStatusListener(new EmotionKeyboard.BoardStatusListener() {
            @Override
            public void onBoradShow() {
                mBottomContainer.setVisibility(View.GONE);
                mInputContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBoradHide() {
                mBottomContainer.setVisibility(View.VISIBLE);
                mInputContainer.setVisibility(View.GONE);
            }
        });
    }

    private void initBottomView() {
        mBottomContainer = (RelativeLayout)mRootView.findViewById(R.id.bottom_container);
        mEmoji1Btn = (ExImageView)mRootView.findViewById(R.id.emoji1_btn);
        mShowInputContainerBtn = (ExTextView)mRootView.findViewById(R.id.show_input_container_btn);
        mEmoji4Btn = (ExImageView)mRootView.findViewById(R.id.emoji4_btn);
        mEmoji3Btn = (ExImageView)mRootView.findViewById(R.id.emoji3_btn);
        mEmoji2Btn = (ExImageView)mRootView.findViewById(R.id.emoji2_btn);
        mShowInputContainerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(U.getCommonUtils().isFastDoubleClick()){
                    return;
                }
                mEmotionKeyboard.showSoftInput();
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }


    @Override
    protected boolean onBackPressed() {
        if (mEmotionKeyboard.isEmotionShown()) {
            mEmotionKeyboard.hideEmotionLayout(false);
            return true;
        }
        return super.onBackPressed();
    }
}
