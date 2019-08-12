package com.zq.mediaengine.kit.bytedance;

import android.content.Context;
import android.opengl.GLES20;

import com.bytedance.labcv.effectsdk.BytedEffectConstants;
import com.bytedance.labcv.effectsdk.RenderManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.engine.Params;
import com.zq.mediaengine.effect.DyEffectResManager;
import com.zq.mediaengine.framework.ImgBufFrame;
import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.kit.ZqEngineKit;
import com.zq.mediaengine.util.gles.GLRender;

/**
 * 头条视频特效封装类。
 * <p>
 * TODO: 实现FBO的复用逻辑，降低内存占用
 * TODO: 加入错误回调
 */

public class BytedEffectFilter {
    private static final String TAG = "BytedEffectFilter";

    private int mOutTexture = ImgTexFrame.NO_TEXTURE;
    private ImgTexFormat mOutFormat;
    private GLRender mGLRender;
    private RenderManager mRenderManager;
    private boolean mInited;

    private int[] mViewPort = new int[4];

    public BytedEffectFilter(GLRender glRender) {
        mGLRender = glRender;
        mGLRender.addListener(mFboCacheClearedListener);

        MyLog.i(TAG, "Create RenderManager");
        mRenderManager = new RenderManager();
        MyLog.i(TAG, "~ RenderManager");
    }

    public SinkPin<ImgTexFrame> getImgTexSinkPin() {
        return mImgTexSinkPin;
    }

    public SinkPin<ImgBufFrame> getImgBufSinkPin() {
        return mImgBufSinkPin;
    }

    public SrcPin<ImgTexFrame> getSrcPin() {
        return mImgTexSrcPin;
    }

    private DyEffectResManager mDyEffectResManager;

    /**
     * 初始化特效句柄
     *
     * @param context     应用上下文
     * @param modelDir    模型文件的根目录，注意不是模型文件的绝对路径，该目录下文件层次和目录名称必须和Demo中提供的完全一致
     * @param licensePath 授权文件绝对路径
     */
    public void init(final Context context, final String modelDir, final String licensePath) {
        if (mInited) {
            MyLog.d(TAG, "has inited cancel this");
            return;
        }
        mGLRender.queueEvent(new Runnable() {
            @Override
            public void run() {
                int ret = mRenderManager.init(context, modelDir, licensePath);
                if (ret == BytedEffectConstants.BytedResultCode.BEF_RESULT_SUC) {
                    mInited = true;
                    MyLog.i(TAG, "init success");
                } else {
                    MyLog.e(TAG, "init failed with modelDir: " + modelDir + " path: " + licensePath);
                }
            }
        });
    }

    /**
     * 初始化
     */
    public void initDyEffects() {
        if (mDyEffectResManager == null) {
            mDyEffectResManager = new DyEffectResManager();
        }
        mDyEffectResManager.tryLoadRes(new DyEffectResManager.Callback() {
            @Override
            public void onResReady(String modelDir, String licensePath) {
                Params config = ZqEngineKit.getInstance().getParams();
                BytedEffectFilter.this.init(U.app(), modelDir, licensePath);
                //初始化美颜相关
                BytedEffectFilter.this.setBeauty(mDyEffectResManager.getBeautyResPath());
                // 磨皮强度
                BytedEffectFilter.this.updateIntensity(BytedEffectConstants.IntensityType.BeautySmooth.getId(), config.getIntensityMopi());
                // 美白强度
                BytedEffectFilter.this.updateIntensity(BytedEffectConstants.IntensityType.BeautyWhite.getId(), config.getIntensityMeibai());
                // 锐化强度
                BytedEffectFilter.this.updateIntensity(BytedEffectConstants.IntensityType.BeautySharp.getId(), config.getIntensityMeibai());
//                    // 腮红强度
//                    mBytedEffectFilter.updateIntensity(BytedEffectConstants.IntensityType.MakeUpBlusher.getId(), mConfig.getIntensitySaihong());
//                    // 唇色强度
//                    mBytedEffectFilter.updateIntensity(BytedEffectConstants.IntensityType.MakeUpLip.getId(), mConfig.getIntensityChunse());
                //初始化瘦脸大眼
                BytedEffectFilter.this.setReshape(mDyEffectResManager.getReshapeResPath());
                BytedEffectFilter.this.updateReshape(config.getIntensityThinFace(), config.getIntensityBigEye());
                // 初始化滤镜
                BytedEffectFilter.this.setFilter(mDyEffectResManager.getFilterResources(config.getNoFilter()));
                BytedEffectFilter.this.updateIntensity(BytedEffectConstants.IntensityType.Filter.getId(), config.getIntensityFilter());
                // 初始化贴纸
                BytedEffectFilter.this.setSticker(mDyEffectResManager.getStickersPath(config.getNoSticker()));
            }
        });
    }

    /**
     * 设置美颜素材
     *
     * @param resourcePath 素材绝对路径 如果传null或者空字符，则取消美颜效果
     */
    public void setBeauty(final String resourcePath) {
        mGLRender.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (!mRenderManager.setBeauty(resourcePath)) {
                    MyLog.e(TAG, "setBeauty failed with path: " + resourcePath);
                }
            }
        });
    }

    /**
     * 设置塑形素材
     *
     * @param resourcePath 素材绝对路径 如果传null或者空字符，则取消塑形效果
     */
    public void setReshape(final String resourcePath) {
        mGLRender.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (!mRenderManager.setReshape(resourcePath)) {
                    MyLog.e(TAG, "setReshape failed with path: " + resourcePath);
                }
            }
        });
    }

    public void setFilter(int no) {
        String path = mDyEffectResManager.getFilterResources(no);
        setFilter(path);
    }

    /**
     * 设置滤镜素材
     *
     * @param resourcePath 素材绝对路径 如果传null或者空字符，则取消滤镜效果
     */
    public void setFilter(final String resourcePath) {
        mGLRender.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (!mRenderManager.setFilter(resourcePath)) {
                    MyLog.e(TAG, "setFilter failed with path: " + resourcePath);
                }
            }
        });
    }

    /**
     * 设置美妆素材
     *
     * @param resourcePath 素材绝对路径 如果传null或者空字符，则取消美妆效果
     */
    public void setMakeUp(final String resourcePath) {
        mGLRender.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (!mRenderManager.setMakeUp(resourcePath)) {
                    MyLog.e(TAG, "setMakeUp failed with path: " + resourcePath);
                }
            }
        });
    }

    public void setSticker(int no) {
        String path = mDyEffectResManager.getStickersPath(no);
        setSticker(path);
    }

    /**
     * 设置贴纸素材
     *
     * @param resourcePath 素材绝对路径 如果传null或者空字符，则取消贴纸效果
     */
    public void setSticker(final String resourcePath) {
        mGLRender.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (!mRenderManager.setSticker(resourcePath)) {
                    MyLog.e(TAG, "setSticker failed with path: " + resourcePath);
                }
            }
        });
    }

    /**
     * 设置强度
     *
     * @param intensitytype 类型
     * @param intensity     0-1
     */
    public void updateIntensity(final int intensitytype, final float intensity) {
        mGLRender.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (!mRenderManager.updateIntensity(intensitytype, intensity)) {
                    MyLog.e(TAG, "updateIntensity failed with type: " + intensitytype + " value: " + intensity);
                }
            }
        });
    }

    /**
     * 设置塑形参数
     *
     * @param cheekintensity 瘦脸强度 0-1
     * @param eyeintensity   大眼参数 0-1
     */
    public void updateReshape(final float cheekintensity, final float eyeintensity) {
        mGLRender.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (!mRenderManager.updateReshape(cheekintensity, eyeintensity)) {
                    MyLog.e(TAG, "updateReshape failed with cheek: " + cheekintensity + " eye: " + eyeintensity);
                }
            }
        });
    }

    public void release() {
        unlockFbo();
        mGLRender.removeListener(mFboCacheClearedListener);
        mRenderManager.release();
        mImgTexSrcPin.disconnect(true);
    }

    private void unlockFbo() {
        mGLRender.queueEvent(new Runnable() {
            @Override
            public void run() {
                mGLRender.getFboManager().unlock(mOutTexture);
                mOutTexture = ImgTexFrame.NO_TEXTURE;
            }
        });
    }

    private GLRender.OnFboCacheClearedListener mFboCacheClearedListener = new GLRender.OnFboCacheClearedListener() {
        @Override
        public void onFboCacheClearedListener() {
            // Fbo cache清空后需要重新获取fbo
            mOutTexture = ImgTexFrame.NO_TEXTURE;
        }
    };

    private SinkPin<ImgBufFrame> mImgBufSinkPin = new SinkPin<ImgBufFrame>() {
        @Override
        public void onFormatChanged(Object format) {
            // do nothing
        }

        @Override
        public void onFrameAvailable(ImgBufFrame frame) {
            // TODO: process buf
        }
    };

    private SinkPin<ImgTexFrame> mImgTexSinkPin = new SinkPin<ImgTexFrame>() {
        @Override
        public void onFormatChanged(Object format) {
            MyLog.d(TAG, "onFormatChanged: " + format);
            if (format == null) {
                return;
            }

            ImgTexFormat inFormat = (ImgTexFormat) format;
            // 处理分辨率变化
            if (mOutFormat != null && (inFormat.width != mOutFormat.width || inFormat.height != mOutFormat.height)) {
                MyLog.i(TAG, "res changed from " + mOutFormat.width + "x" + mOutFormat.height +
                        " to " + inFormat.width + "x" + inFormat.height);
                if (mOutTexture != ImgTexFrame.NO_TEXTURE) {
                    mGLRender.getFboManager().remove(mOutTexture);
                    mOutTexture = ImgTexFrame.NO_TEXTURE;
                }
            }
            mOutFormat = new ImgTexFormat(inFormat);
            mImgTexSrcPin.onFormatChanged(mOutFormat);
        }

        @Override
        public void onFrameAvailable(ImgTexFrame frame) {
            if (!mInited) {
                mImgTexSrcPin.onFrameAvailable(frame);
                return;
            }

            if (mOutTexture == ImgTexFrame.NO_TEXTURE) {
                mOutTexture = mGLRender.getFboManager().getTextureAndLock(mOutFormat.width, mOutFormat.height);
            }
            GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, mViewPort, 0);
            GLES20.glViewport(0, 0, frame.format.width, frame.format.height);
            boolean suc = mRenderManager.process(frame.textureId, mOutTexture, mOutFormat.width,
                    mOutFormat.height, BytedEffectConstants.Rotation.CLOCKWISE_ROTATE_0);
            // reset gl state
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(mViewPort[0], mViewPort[1], mViewPort[2], mViewPort[3]);
            int outTex = mOutTexture;
            if (!suc) {
                MyLog.e(TAG, "process failed!");
                outTex = frame.textureId;
            }
            ImgTexFrame outFrame = new ImgTexFrame(mOutFormat, outTex, null, frame.pts);
            mImgTexSrcPin.onFrameAvailable(outFrame);
        }

        @Override
        public synchronized void onDisconnect(boolean recursive) {
            if (recursive) {
                release();
            } else {
                unlockFbo();
            }
        }
    };

    private SrcPin<ImgTexFrame> mImgTexSrcPin = new SrcPin<>();
}
