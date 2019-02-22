package com.module.playways.rank.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.component.busilib.view.BitmapTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.RoomData;
import com.module.rank.R;

/**
 * 某个人的战绩
 */
public class RankResultView extends RelativeLayout {

    RelativeLayout mAvatarArea;
    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ExTextView mSongTv;
    RelativeLayout mResultArea;
    SimpleDraweeView mAvatarIvFirst;
    ExTextView mFirstResultTv;
    SimpleDraweeView mAvatarIvSecond;
    ExTextView mSecondResultTv;
    SimpleDraweeView mAvatarIvThree;
    ExTextView mThirdResultTv;
    RelativeLayout mScoreArea;
    BitmapTextView mPkScore;

    public RankResultView(Context context) {
        super(context);
        init();
    }

    public RankResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RankResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.rank_result_view_layout, this);

        mAvatarArea = (RelativeLayout) findViewById(R.id.avatar_area);
        mAvatarIv = (SimpleDraweeView) findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) findViewById(R.id.name_tv);
        mSongTv = (ExTextView) findViewById(R.id.song_tv);
        mResultArea = (RelativeLayout) findViewById(R.id.result_area);
        mAvatarIvFirst = (SimpleDraweeView) findViewById(R.id.avatar_iv_first);
        mFirstResultTv = (ExTextView) findViewById(R.id.first_result_tv);
        mAvatarIvSecond = (SimpleDraweeView) findViewById(R.id.avatar_iv_second);
        mSecondResultTv = (ExTextView) findViewById(R.id.second_result_tv);
        mAvatarIvThree = (SimpleDraweeView) findViewById(R.id.avatar_iv_three);
        mThirdResultTv = (ExTextView) findViewById(R.id.third_result_tv);
        mScoreArea = (RelativeLayout) findViewById(R.id.score_area);
        mPkScore = (BitmapTextView) findViewById(R.id.pk_score);
        mPkScore.setText("11.5");

    }

    //绑定数据
    public void bindData(RoomData roomData, int useId) {


    }


}
