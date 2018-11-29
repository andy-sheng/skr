package com.changba.songstudio;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.changba.songstudio.recording.RecordingImplType;
import com.changba.songstudio.recording.camera.preview.ChangbaRecordingPreviewScheduler;
import com.changba.songstudio.recording.camera.preview.ChangbaRecordingPreviewView;
import com.changba.songstudio.recording.camera.preview.ChangbaVideoCamera;
import com.changba.songstudio.recording.exception.RecordingStudioException;
import com.changba.songstudio.recording.exception.StartRecordingException;
import com.changba.songstudio.recording.service.PlayerService;
import com.changba.songstudio.recording.video.CommonVideoRecordingStudio;
import com.changba.songstudio.recording.video.VideoRecordingStudio;
import com.common.log.MyLog;
import com.common.utils.U;
import com.engine.Params;

import java.io.File;

public class CbEngineAdapter {
    public final static String TAG = "CbEngineAdapter";


    private static class CbEngineAdapterHolder {
        private static final CbEngineAdapter INSTANCE = new CbEngineAdapter();
    }

    private CbEngineAdapter() {
        MyLog.d(TAG, "CbEngineAdapter");
        try {
            System.loadLibrary("cb-engine");
        } catch (Exception e) {
            e.printStackTrace();
            MyLog.w(TAG, e);
        }
    }

    public static final CbEngineAdapter getInstance() {
        return CbEngineAdapterHolder.INSTANCE;
    }

    private Params mParams;
    private CommonVideoRecordingStudio recordingStudio;
    private ChangbaVideoCamera mChangbaVideoCamera;
    private ChangbaRecordingPreviewScheduler mPreviewScheduler;

    private Handler mUiHandler = new Handler();

    private Handler mTimeHandler = new Handler() {
        public void handleMessage(Message msg) {
            // 计算当前时间
            int currentTimeMs = Math.max(msg.arg1, 0);
            int accompanyTimeMs = Math.max(msg.arg2, 0);
//            MyLog.d(TAG, "mTimeHandler handleMessage currentTimeMs=" + currentTimeMs + " accompanyTimeMs:" + accompanyTimeMs);

        }
    };

    private PlayerService.OnCompletionListener onComletionListener = new PlayerService.OnCompletionListener() {
        @Override
        public void onCompletion() {
            MyLog.d(TAG, "onCompletion");
//            iscompleted = true;
//            complete_handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Log.i("problem", "onCompletion");
//                    if (song.getType() == SongSelectionActivity.BGM_TYPE) {
//                        Log.i("problem", "song.getType() == SongSelectionActivity.BGM_TYPE showMiniPlayer");
//                        // 伴奏
//                        showMiniPlayer();
//                    } else {
//                        Log.i("problem", "onCompletion  hideMiniPlayer");
//                        hideMiniPlayer();
//                    }
//                }
//            });
        }
    };

    private VideoRecordingStudio.RecordingStudioStateCallback recordingStudioStateCallback = new VideoRecordingStudio.RecordingStudioStateCallback() {
        @Override
        public void onConnectRTMPServerFailed() {
            U.getToastUtil().showShort("连接RTMP服务器失败");
        }

        @Override
        public void onConnectRTMPServerSuccessed() {
            MyLog.d(TAG, "onConnectRTMPServerSuccessed");
        }

        @Override
        public void onStartRecordingException(final StartRecordingException exception) {
            MyLog.d(TAG, exception);
        }

        @Override
        public void onPublishTimeOut() {
            U.getToastUtil().showShort("推流期间，由于网络原因超时");
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopRecord();
                }
            });
        }

        @Override
        public void statisticsCallback(long startTimeMills, int connectTimeMills, int publishDurationInSec,
                                       float discardFrameRatio, float publishAVGBitRate, float expectedBitRate, String adaptiveBitrateChart) {
            StringBuilder statisticsBuilder = new StringBuilder("statisticsCallback : ");
            statisticsBuilder.append("startTimeMills【").append(startTimeMills).append("】");
            statisticsBuilder.append("connectTimeMills【").append(connectTimeMills).append("】");
            statisticsBuilder.append("publishDurationInSec【").append(publishDurationInSec).append("】");
            statisticsBuilder.append("discardFrameRatio【").append(discardFrameRatio).append("】");
            statisticsBuilder.append("publishAVGBitRate【").append(publishAVGBitRate).append("】");
            statisticsBuilder.append("expectedBitRate【").append(expectedBitRate).append("】");
            statisticsBuilder.append("adaptiveBitrateChart【").append(adaptiveBitrateChart).append("】");
            Log.i("problem", statisticsBuilder.toString());
        }

        @Override
        public void adaptiveVideoQuality(int videoQuality) {
            boolean invalidFlag = false;
            boolean showUserTip = true;
            int bitrate = VideoRecordingStudio.COMMON_VIDEO_BIT_RATE;
            int bitrateLimits = VideoRecordingStudio.COMMON_VIDEO_BIT_RATE;
            int fps = VideoRecordingStudio.VIDEO_FRAME_RATE;
            switch (videoQuality) {
                case VideoRecordingStudio.HIGHT_VIDEO_QUALITY:
                    bitrate = VideoRecordingStudio.COMMON_VIDEO_BIT_RATE;
                    ;
                    bitrateLimits = VideoRecordingStudio.COMMON_VIDEO_BIT_RATE;
                    ;
                    fps = VideoRecordingStudio.VIDEO_FRAME_RATE;
                    ;
                    break;
                case VideoRecordingStudio.MIDDLE_VIDEO_QUALITY:
                    bitrate = VideoRecordingStudio.MIDDLE_VIDEO_BIT_RATE;
                    bitrateLimits = VideoRecordingStudio.MIDDLE_VIDEO_BIT_RATE;
                    fps = VideoRecordingStudio.MIDDLE_VIDEO_FRAME_RATE;
                    break;
                case VideoRecordingStudio.LOW_VIDEO_QUALITY:
                    showUserTip = true;
                    bitrate = VideoRecordingStudio.LOW_VIDEO_BIT_RATE;
                    bitrateLimits = VideoRecordingStudio.LOW_VIDEO_BIT_RATE;
                    fps = VideoRecordingStudio.LOW_VIDEO_FRAME_RATE;
                    break;
                case VideoRecordingStudio.INVLAID_QUALITY:
                    invalidFlag = true;
                    break;
                default:
                    break;
            }
            if (invalidFlag) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        stopRecord();
                        U.getToastUtil().showShort("由于当前网络环境过差，无法支持视频直播。请切换至其他网络或改善所处网络环境后重新开播！");
                    }
                });
            } else {
                Log.i("problem", "由于当前网络环境较差，已切换至流畅模式。如需使用高清模式，请改善所处网络环境后重新开播！[" + (int) (bitrate / 1024) + "Kbps, " + fps + "]");
                if (showUserTip) {
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String text = "由于当前网络环境较差，已切换至流畅模式。如需使用高清模式，请改善所处网络环境后重新开播！";
                            U.getToastUtil().showShort(text);
                        }
                    });
                }
                mPreviewScheduler.adaptiveVideoQuality(bitrateLimits, bitrate, fps);
            }
        }

        @Override
        public void hotAdaptiveVideoQuality(int maxBitrate, int avgBitrate, int fps) {
            MyLog.d(TAG, "hotAdaptiveVideoQuality" + " maxBitrate=" + maxBitrate + " avgBitrate=" + avgBitrate + " fps=" + fps);
            mPreviewScheduler.hotConfigQuality(maxBitrate * 1000, avgBitrate * 1000, fps);
        }

        @Override
        public void statisticsBitrateCallback(int sendBitrate, int compressedBitrate) {
            MyLog.d(TAG, "statisticsBitrateCallback" + " sendBitrate=" + sendBitrate + " compressedBitrate=" + compressedBitrate);
        }
    };

    public void init(Params config) {
        mParams = config;
    }

    private void tryInitPreview(SurfaceView surfaceView) {
        if (mPreviewScheduler == null) {
            synchronized (this) {
                if (mPreviewScheduler == null) {
                    mChangbaVideoCamera = new ChangbaVideoCamera(surfaceView.getContext());
                    ChangbaRecordingPreviewView previewView = new ChangbaRecordingPreviewView(surfaceView);
                    mPreviewScheduler = new ChangbaRecordingPreviewScheduler(previewView, mChangbaVideoCamera) {
                        public void onPermissionDismiss(final String tip) {
                            U.getToastUtil().showShort(tip);
                        }
                    };
                }
            }
        }
    }

    private void tryInitRecording() {
        recordingStudio = new CommonVideoRecordingStudio(RecordingImplType.ANDROID_PLATFORM,
                mTimeHandler, onComletionListener, recordingStudioStateCallback);
        try {
            recordingStudio.initRecordingResource(mPreviewScheduler);
        } catch (RecordingStudioException e) {
            recordingStudio.destroyRecordingResource();
            e.printStackTrace();
        }
    }

    /**
     * 开启相机预览
     *
     * @param surfaceView
     */
    public void startPreview(final SurfaceView surfaceView) {
        tryInitPreview(surfaceView);
        mPreviewScheduler.startPreview("CbEngineAdapter");
    }


    /**
     * 关闭预览
     */
    public void stopPreview() {
        if (mPreviewScheduler != null) {
            mPreviewScheduler.stop();
        }
    }

    /**
     * 开始采集视频流
     */
    public void startRecord() {
        if (mPreviewScheduler == null) {
            throw new IllegalStateException("必须设置一个视频输入源");
        }
        tryInitRecording();
        int adaptiveBitrateWindowSizeInSecs = 3;
        int adaptiveBitrateEncoderReconfigInterval = 15 * 1000;
        int adaptiveBitrateWarCntThreshold = 10;

        //  width 和 height 一定要是 2 的倍数，不然MediaCodec会崩溃
        int width = mParams.getLocalVideoWidth();
        int height = mParams.getLocalVideoHeight();
        int bitRateKbs = 900;
        int audioSampleRate = recordingStudio.getRecordSampleRate();
        // 必须先使用硬编码
        boolean isUseHWEncoder = true;
        File outFilePath = new File(Environment.getExternalStorageDirectory(), "changba.mp4");

        recordingStudio.startVideoRecording(outFilePath.getPath(), bitRateKbs, width, height,
                audioSampleRate, 0, adaptiveBitrateWindowSizeInSecs, adaptiveBitrateEncoderReconfigInterval,
                adaptiveBitrateWarCntThreshold, 300, 800, isUseHWEncoder);
    }

    /**
     * 停止采集视频流
     */
    public void stopRecord() {
        recordingStudio.stopRecording();
    }

    public void destroy() {
        if (mChangbaVideoCamera != null) {
            mChangbaVideoCamera.releaseCamera();
        }
        mChangbaVideoCamera = null;
        if (mPreviewScheduler != null) {
            mPreviewScheduler.stop();
        }
        mPreviewScheduler = null;
        if (recordingStudio != null) {
            recordingStudio.stopRecording();
        }
        recordingStudio = null;
    }

}
