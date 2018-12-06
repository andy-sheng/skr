package com.module.rankingmode.song.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.common.view.ex.ExTextView;
import com.module.rankingmode.R;

public class SongSelecterTabView extends BaseSelecterView {
    public SongSelecterTabView(Context context) {
        this(context, null);
    }

    public SongSelecterTabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SongSelecterTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.song_select_tab_view, this);
        ExTextView tvSongRecommend = findViewById(R.id.tv_song_recommend);
        ExTextView tvSongHistory = findViewById(R.id.tv_song_history);
        initSelecterView(new View[]{tvSongRecommend, tvSongHistory}, 0);
    }

    OnSelectSongTypeListener mOnSelectSongTypeListener;

    public void setOnSelectSongTypeListener(OnSelectSongTypeListener onSelectSongTypeListener){
        mOnSelectSongTypeListener = onSelectSongTypeListener;
    }

    @Override
    public OnSelectListener getOnSelectListener() {
        return onSelectListener;
    }

    public OnSelectListener onSelectListener = new OnSelectListener() {
        @Override
        public void onSelect(View view) {
            if(R.id.tv_song_recommend == view.getId()){
                if(mOnSelectSongTypeListener != null){
                    mOnSelectSongTypeListener.onSelectRecommend();
                }
            } else if(R.id.tv_song_history == view.getId()){
                if(mOnSelectSongTypeListener != null){
                    mOnSelectSongTypeListener.onSelectHistory();
                }
            }
        }
    };

    public interface OnSelectSongTypeListener{
        void onSelectRecommend();

        void onSelectHistory();
    }
}
