package com.engine.arccloud;

import java.util.List;

public interface ArcRecognizeListener {
    void onResult(String result, List<SongInfo> list, SongInfo targetSongInfo);
}
