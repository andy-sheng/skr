package com.module.playways.rank.song.callback;

import com.kingja.loadsir.callback.Callback;
import com.module.rank.R;

public class SongEmptyCallback extends Callback {
    @Override
    protected int onCreateView() {
        return R.layout.history_song_empty_layout;
    }
}
