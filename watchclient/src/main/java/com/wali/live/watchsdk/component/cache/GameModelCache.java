package com.wali.live.watchsdk.component.cache;

import android.util.LruCache;

import com.wali.live.watchsdk.component.viewmodel.GameViewModel;

/**
 * Created by lan 2017/7/28.
 */
public class GameModelCache {
    private static final int MAX_CAPACITY = 20;

    private static LruCache<String, GameViewModel> sLruCache = new LruCache(MAX_CAPACITY);

    public static GameViewModel getGameModel(String gameId) {
        return sLruCache.get(gameId);
    }

    public static boolean addGameModel(GameViewModel gameViewModel) {
        if (gameViewModel == null) {
            return false;
        }
        sLruCache.put(gameViewModel.getGameId(), gameViewModel);
        return true;
    }
}
