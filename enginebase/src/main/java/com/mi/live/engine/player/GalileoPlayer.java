package com.mi.live.engine.player;

import android.content.Context;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.base.global.GlobalData;
import com.base.thread.ThreadPool;
import com.base.utils.display.DisplayUtils;
import com.mi.live.engine.base.GalileoDeviceManager;
import com.mi.live.engine.media.player.IMediaPlayer;
import com.mi.live.engine.media.player.IjkMediaPlayer;
import com.xiaomi.player.Player;
import com.xiaomi.player.enums.PlayerWorkingMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by chenyong on 16/7/11.
 *
 * @module 拉流模块，实现引擎拉流功能
 */
public class GalileoPlayer implements IPlayer {
    private static final String DEFAULT_PORT = "80";

    private IjkMediaPlayer mIjkMediaPlayer;

    public GalileoPlayer(final Context context, final PlayerWorkingMode mode, final long observer, final String tagInfo) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                GalileoDeviceManager.INSTANCE.init(context);
                String tag = TextUtils.isEmpty(tagInfo) ? "" : tagInfo;
                mIjkMediaPlayer = new IjkMediaPlayer(context, tag, mode, observer);
                mIjkMediaPlayer.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspectFit, GlobalData.screenWidth, GlobalData.screenHeight);
                int curMargin = (GlobalData.screenHeight - GlobalData.screenWidth * 9 / 16) / 2;
                int targetMargin = DisplayUtils.dip2px(140);
                float distance = curMargin - targetMargin;
                mIjkMediaPlayer.shiftUp(distance * 2 / GlobalData.screenHeight);
            }
        }, "GalileoPlayer()");
    }

    @Override
    public void setBufferTimeMax(final float timeSecond) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.setBufferTimeMax((long) timeSecond);
            }
        }, "setBufferTimeMax");
    }

    @Override
    public void reload(final String path, final boolean flushBuffer) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.reload(path, flushBuffer);
            }
        }, "reload");
    }

    @Override
    public void setSurface(final Surface surface) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.setSurface(surface);
            }
        }, "setSurface");
    }

    @Override
    public void setDisplay(final SurfaceHolder sh) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.setDisplay(sh);
            }
        }, "setDisplay");
    }

    @Override
    public void setTimeout(int prepareTimeout, int readTimeout) {

    }

    @Override
    public void setLogPath(String path) {

    }

    @Override
    public void setOnPreparedListener(final IMediaPlayer.OnPreparedListener listener) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.setOnPreparedListener(listener);
            }
        }, "setOnPreparedListener");
    }

    @Override
    public void setOnCompletionListener(final IMediaPlayer.OnCompletionListener listener) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.setOnCompletionListener(listener);
            }
        }, "setOnCompletionListener");
    }

    @Override
    public void setOnBufferingUpdateListener(final IMediaPlayer.OnBufferingUpdateListener listener) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.setOnBufferingUpdateListener(listener);
            }
        }, "setOnBufferingUpdateListener");
    }

    @Override
    public void setOnSeekCompleteListener(final IMediaPlayer.OnSeekCompleteListener listener) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.setOnSeekCompleteListener(listener);
            }
        }, "setOnSeekCompleteListener");
    }

    @Override
    public void setOnVideoSizeChangedListener(final IMediaPlayer.OnVideoSizeChangedListener listener) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.setOnVideoSizeChangedListener(listener);
            }
        }, "setOnVideoSizeChangedListener");
    }

    @Override
    public void setOnErrorListener(final IMediaPlayer.OnErrorListener listener) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.setOnErrorListener(listener);
            }
        }, "setOnErrorListener");
    }

    @Override
    public void setOnInfoListener(final IMediaPlayer.OnInfoListener listener) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.setOnInfoListener(listener);
            }
        }, "setOnInfoListener");
    }

    @Override
    public void setDataSource(final String path, final String host) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                try {
                    mIjkMediaPlayer.setDataSource(path, host);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "setDataSource");
    }

    @Override
    public void setScreenOnWhilePlaying(final boolean screenOn) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.setScreenOnWhilePlaying(screenOn);
            }
        }, "setScreenOnWhilePlaying");
    }

    @Override
    public void prepareAsync(final boolean realTime) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.prepareAsync(realTime);
            }
        }, "prepareAsync");
    }

    @Override
    public int getVideoWidth() {
        if (mIjkMediaPlayer != null) {
            return mIjkMediaPlayer.getVideoWidth();
        }
        return 0;
    }

    @Override
    public int getVideoHeight() {
        if (mIjkMediaPlayer != null) {
            return mIjkMediaPlayer.getVideoHeight();
        }
        return 0;
    }

    @Override
    public void seekTo(final long msec) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.seekTo(msec);
            }
        }, "seekTo");
    }

    @Override
    public void start() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.start();
            }
        }, "start");
    }

    @Override
    public void reset() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.reset();
            }
        }, "reset");
    }

    @Override
    public boolean isPlaying() {
        return mIjkMediaPlayer != null && mIjkMediaPlayer.isPlaying();
    }

    @Override
    public void pause() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.pause();
            }
        }, "pause");
    }

    @Override
    public long getDuration() {
        if (mIjkMediaPlayer != null) {
            return mIjkMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public void stop() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.stop();
            }
        }, "stop");
    }

    @Override
    public void release() {
        if (mIjkMediaPlayer != null) {
            mIjkMediaPlayer.resetListeners();
        }
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.release();
                mIjkMediaPlayer = null;
                GalileoDeviceManager.INSTANCE.destroy();
            }
        }, "release");
    }

    @Override
    public String getServerAddress() {
        if (mIjkMediaPlayer != null) {
            return mIjkMediaPlayer.getDataSource();
        }
        return "";
    }

    @Override
    public long getCurrentPosition() {
        if (mIjkMediaPlayer != null) {
            return mIjkMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void setVolume(final float volumeL, final float volumeR) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.setVolume(volumeL, volumeR);
            }
        }, "setVolume");
    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public long getCurrentStreamPosition() {
        if (mIjkMediaPlayer != null) {
            return mIjkMediaPlayer.getCurrentStreamPosition();
        }
        return 0;
    }

    @Override
    public void setGravity(final Player.SurfaceGravity gravity, final int width, final int height) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.setGravity(gravity, width, height);
            }
        }, "setGravity");
    }

    @Override
    public void shiftUp(final float ratio) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mIjkMediaPlayer.shiftUp(ratio);
            }
        }, "shiftUp");
    }

    @Override
    public long getStreamId() {
        if (mIjkMediaPlayer != null) {
            return mIjkMediaPlayer.getStreamId();
        }
        return 0;
    }

    @Override
    public long getAudioSource() {
        if (mIjkMediaPlayer != null) {
            return mIjkMediaPlayer.getAudioSource();
        }
        return 0;
    }

    private String getDefaultPortForUrl(String url) {
        if (!TextUtils.isEmpty(url) && url.startsWith("http://")) {
            return ":" + DEFAULT_PORT;
        } else {
            return "";
        }
    }

    private String[] ipListToIpArray(List<String> ipList) {
        if (ipList != null && !ipList.isEmpty()) {
            LinkedHashSet<String> ipSet = new LinkedHashSet<>();
            ipSet.addAll(ipList);
            String[] ipArray = new String[ipSet.size()];
            int i = 0;
            for (String ip : ipSet) {
                ipArray[i++] = ip.contains(":") ? ip : (ip + getDefaultPortForUrl(mIjkMediaPlayer.getDataSource()));
            }
            return ipArray;
        } else {
            return new String[0];
        }
    }

    @Override
    public void setIpList(final List<String> httpIpList, final List<String> localIpList) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                List<String> tmpIpList = null;
                if (httpIpList != null && !httpIpList.isEmpty() && localIpList != null && !localIpList.isEmpty()) {
                    tmpIpList = new ArrayList<>();
                    tmpIpList.addAll(localIpList);
                    tmpIpList.removeAll(httpIpList);
                }
                mIjkMediaPlayer.setIpList(ipListToIpArray(httpIpList), ipListToIpArray(tmpIpList));
            }
        }, "setIpList");
    }
}
