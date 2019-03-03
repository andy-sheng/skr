package com.module.playways.rank.song.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.module.rank.R;

public class SearchFeedbackView extends RelativeLayout {

    NoLeakEditText mSongName;
    NoLeakEditText mSongSinger;
    ExTextView mCancelTv;
    ExTextView mConfirmTv;

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
        mCancelTv = (ExTextView) findViewById(R.id.cancel_tv);
        mConfirmTv = (ExTextView) findViewById(R.id.confirm_tv);
    }

    public String getSongName() {
        return mSongName.getText().toString().trim();
    }

    public String getSongSinger() {
        return mSongSinger.getText().toString().trim();
    }
}
