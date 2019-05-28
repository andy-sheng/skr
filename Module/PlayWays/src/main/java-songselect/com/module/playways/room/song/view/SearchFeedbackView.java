package com.module.playways.room.song.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.NoLeakEditText;
import com.dialog.view.StrokeTextView;
import com.module.playways.R;
import com.zq.toast.CommonToastView;

public class SearchFeedbackView extends RelativeLayout {

    View mPlaceBottomView;
    View mPlaceTopView;

    NoLeakEditText mSongName;
    NoLeakEditText mSongSinger;
    StrokeTextView mCancelTv;
    StrokeTextView mConfirmTv;

    BaseFragment mFragment;

    Listener mListener;

    public SearchFeedbackView(BaseFragment fragment) {
        super(fragment.getContext());
        this.mFragment = fragment;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.search_feedback_view, this);

        mSongName = (NoLeakEditText) findViewById(R.id.song_name);
        mSongSinger = (NoLeakEditText) findViewById(R.id.song_singer);
        mCancelTv = (StrokeTextView) findViewById(R.id.cancel_tv);
        mConfirmTv = (StrokeTextView) findViewById(R.id.confirm_tv);

        mPlaceBottomView = (View) findViewById(R.id.place_bottom_view);
        mPlaceTopView = (View) findViewById(R.id.place_top_view);

        ViewGroup.LayoutParams layoutParams = mPlaceBottomView.getLayoutParams();
        layoutParams.height = U.getKeyBoardUtils().getKeyBoardHeight();
        mPlaceBottomView.setLayoutParams(layoutParams);

        mCancelTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickCancle();
                }
            }
        });

        mConfirmTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    String songName = mSongName.getText().toString().trim();
                    String songSinger = mSongSinger.getText().toString().trim();
                    if (TextUtils.isEmpty(songName) && TextUtils.isEmpty(songSinger)) {
                        U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                                .setImage(R.drawable.touxiangshezhishibai_icon)
                                .setText("请输入歌曲名或歌手名")
                                .build());
                    } else {
                        mListener.onClickSubmit(songName, songSinger);
                    }
                }
            }
        });

        mPlaceBottomView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickCancle();
                }
            }
        });

        mPlaceTopView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickCancle();
                }
            }
        });

        mSongName.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSongName.requestFocus();
                U.getKeyBoardUtils().showSoftInputKeyBoard(mFragment.getActivity());
            }
        }, 300);

    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onClickSubmit(String songName, String songSinger);

        void onClickCancle();
    }
}
