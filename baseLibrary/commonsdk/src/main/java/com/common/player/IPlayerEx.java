package com.common.player;

import android.view.Surface;

/**
 * Created by yangli on 2017/9/20.
 */
public interface IPlayerEx {

    void addCallback(String from,IPlayerCallback callback);

    void removeCallback(String from);

    boolean startPlay(String from,String path); //true 是重头播，false 为继续播

    void pause(String from);

    void resume(String from);

    void stop(String from);

    void reset(String from);

    void release(String from);

    void seekTo(String from,long msec);

}
