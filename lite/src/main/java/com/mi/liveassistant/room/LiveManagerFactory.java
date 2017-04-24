package com.mi.liveassistant.room;

/**
 * Created by lan on 17/4/24.
 */
public class LiveManagerFactory {
    public static LiveManagerFactory.Builder newNormalLiveManager() {
        return new LiveManagerFactory.Builder().newNormalInstance();
    }

    public static LiveManagerFactory.Builder newGameLiveManager() {
        return new LiveManagerFactory.Builder().newGameInstance();
    }

    public static class Builder {
        private BaseLiveManager mBaseLiveManager;

        private Builder() {
        }

        protected LiveManagerFactory.Builder newNormalInstance() {
            mBaseLiveManager = new NormalLiveManager();
            return this;
        }

        protected LiveManagerFactory.Builder newGameInstance() {
            mBaseLiveManager = new GameLiveManager();
            return this;
        }

        public BaseLiveManager build() {
            return mBaseLiveManager;
        }
    }


}
