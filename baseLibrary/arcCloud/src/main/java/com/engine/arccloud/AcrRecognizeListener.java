package com.engine.arccloud;

import java.util.List;

public interface AcrRecognizeListener {
    void onResult(String result, List<SongInfo> list, SongInfo targetSongInfo,int lineNo);
}
