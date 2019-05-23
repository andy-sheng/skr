package com.module.playways.room.song.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.dialog.view.StrokeTextView;
import com.module.playways.R;

public class SearchFeedbackView extends RelativeLayout {

    NoLeakEditText mSongName;
    NoLeakEditText mSongSinger;
    StrokeTextView mCancelTv;
    StrokeTextView mConfirmTv;

    Listener mListener;

    public SearchFeedbackView(Context context) {
        super(context);
        init();
    }

    public SearchFeedbackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchFeedbackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.search_feedback_view, this);

        mSongName = (NoLeakEditText) findViewById(R.id.song_name);
        mSongSinger = (NoLeakEditText) findViewById(R.id.song_singer);
        mCancelTv = (StrokeTextView) findViewById(R.id.cancel_tv);
        mConfirmTv = (StrokeTextView) findViewById(R.id.confirm_tv);

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
                    mListener.onClickSubmit(songName, songSinger);
                }
            }
        });
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onClickSubmit(String songName, String songSinger);

        void onClickCancle();
    }
}
