package com.zq.mediaengine.filter.imgtex;

import android.opengl.GLES20;
import android.text.TextUtils;

import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.util.CredtpWrapper;
import com.zq.mediaengine.util.gles.GLRender;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * effect filter base
 * 渐变滤镜、多texture输入滤镜基类
 */

public abstract class ImgEffectFilterBase extends ImgTexFilter {
    private static final String TAG = "ImgEffectFilterBase";

    private static final int UPDATE_INTERVAL = 20;  //渐变因子刷新频率
    public static final int GRADIENT_FACTOR_TYPE_UP = 1;  //渐变因子从小变大
    public static final int GRADIENT_FACTOR_TYPE_DOWN = 2; //渐变因子从大变小
    public static final int GRADIENT_FACTOR_TYPE_UP_DOWN = 3;  //渐变因子先从小变大，再从大变小

    public static final int GRADIENT_TYPE_1 = 1;  //Uniform1
    public static final int GRADIENT_TYPE_2 = 2;  //Uniform2fv

    private String mVertexShader = BASE_VERTEX_SHADER;
    private String mFragmentShader;

    //gradient factor
    private float mGradientFactorMinValue = 0f;
    private float mGradientFactorMaxValue = 1f;
    private int mGradientFactorFrameCount = 30;  //渐变因子持续的帧数，以帧数做渐变周期时使用
    private float mGradientFactorUnitValue = mGradientFactorMaxValue / mGradientFactorFrameCount;
    private float mGradientFactorDuration = 1000;  //渐变因子默认持续时间1s，以时间做渐变周期时使用
    private float mGradientFactorMaxCount = mGradientFactorDuration / UPDATE_INTERVAL;  //渐变因子一个周期刷新最大次数
    private float mGradientFactorValue = mGradientFactorMinValue;
    private int mGradientFactorType = GRADIENT_FACTOR_TYPE_UP;

    //gradient
    private int mGradientLoc = -1;
    private String mGradientName;
    private int mGradientType = GRADIENT_TYPE_1;

    //滤镜渐变效果timer,不依赖主视频，自启动渐变timer
    private Timer mFilterTimer;
    private TimerTask mFilterTimerTask;
    private int mIndex;
    private boolean mEffectAuto = false;  //以帧数做渐变周期时设置

    private Map<Integer, TextureInfo> mVTexInfos;  //key:VTexLoc, value:VTex

    public ImgEffectFilterBase(GLRender glRender) {
        super(glRender);
        init(mVertexShader, mFragmentShader);

        mVTexInfos = new LinkedHashMap<>();
        for (int i = 0; i < getVSinkPinNum(); i++) {
            mVTexInfos.put(i, new TextureInfo("vTexture" + String.valueOf(i + 1)));
        }
    }

    public void setVertex(String vertex) {
        mVertexShader = vertex;
        init(mVertexShader, mFragmentShader);
    }

    public void setFragment(int fragmentType) {
        mFragmentShader = CredtpWrapper.getInstance().getCredtpByType(fragmentType);
        init(mVertexShader, mFragmentShader);
    }

    public void setFragment(String fragment) {
        mFragmentShader = fragment;
        init(mVertexShader, mFragmentShader);
    }

    @Override
    public int getSinkPinNum() {
        return getVSinkPinNum() + 1;
    }

    abstract protected int getVSinkPinNum();

    public SinkPin<ImgTexFrame> getVSinkPin(int index) {
        return getSinkPin(index + 1);
    }

    abstract protected float[] getGradientValue();

    /**
     * 渐变因子的变更曲线
     *
     * @param type default value {@link #GRADIENT_FACTOR_TYPE_UP}
     * @see #GRADIENT_FACTOR_TYPE_DOWN
     * @see #GRADIENT_FACTOR_TYPE_UP_DOWN
     */
    public void setGradientFactorType(int type) {
        mGradientFactorType = type;
    }

    public int getGradientFactorType() {
        return mGradientFactorType;
    }

    public void setMAXGradientFactorValue(float maxValue) {
        mGradientFactorMaxValue = maxValue;
    }

    public float getMAXGradientFactorValue() {
        return mGradientFactorMaxValue;
    }

    public void setTimeInfoEffectDuration(long duration) {
        mGradientFactorDuration = duration;
        mGradientFactorMaxCount = (int) duration / UPDATE_INTERVAL;
    }

    public void setGradientFactorFrameCount(int framesCount) {
        mGradientFactorFrameCount = framesCount;
        mGradientFactorUnitValue = mGradientFactorMaxValue / framesCount;

    }

    public void setEffectAuto(boolean auto) {
        mEffectAuto = auto;
    }

    /**
     * 设置渐变因子的值
     *
     * @param value
     */
    public void setGradientFactorValue(float value) {
        mGradientFactorValue = value;
    }

    public float getGradientFactorValue() {
        return mGradientFactorValue;
    }

    /**
     * 渐变值在shader中的类型
     *
     * @param type default value {@link #GRADIENT_TYPE_1}
     * @see #GRADIENT_TYPE_2
     */
    public void setGradientType(int type) {
        mGradientType = type;
    }

    /**
     * 渐变值在shader中的命名
     *
     * @param gradientName
     */
    public void setGradientName(String gradientName) {
        mGradientName = gradientName;
    }

    /**
     * 作用于filter的参数渐变效果
     *
     * @hide
     */
    public void startFilterTime() {
        final float unitValue = mGradientFactorMaxValue / mGradientFactorMaxCount;

        mIndex = 0;
        mFilterTimerTask = new TimerTask() {
            @Override
            public void run() {
                setGradientFactorValue(mIndex * unitValue);
                mIndex++;
                if (mIndex >= mGradientFactorMaxCount) {
                    mIndex = 0;
                }
            }
        };
        mFilterTimer = new Timer();
        mFilterTimer.schedule(mFilterTimerTask, 0, UPDATE_INTERVAL);
    }

    /**
     * 停止滤镜的渐变效果
     *
     * @hide
     */
    public void stopFilterTime() {
        if (mFilterTimer != null) {
            mFilterTimer.cancel();
            mFilterTimer = null;

            setGradientFactorValue(0f);
            mIndex = 0;
        }
    }

    @Override
    protected void onFormatChanged(final ImgTexFormat format) {

    }

    @Override
    public void onDrawArraysPre() {
        if (mGradientLoc >= 0) {
            switch (mGradientType) {
                case GRADIENT_TYPE_1:
                    GLES20.glUniform1f(mGradientLoc, getGradientValue()[0]);
                    break;
                case GRADIENT_TYPE_2:
                    GLES20.glUniform2fv(mGradientLoc, 1, getGradientValue(), 0);
                    break;
            }
        }

        for (int i = 0; i < getVSinkPinNum(); i++) {
            TextureInfo textureInfo = mVTexInfos.get(i);
            if (textureInfo.textureLoc >= 0) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo.texture[0]);
                GLES20.glUniform1i(textureInfo.textureLoc, 2);
            }
        }
        //以帧数做渐变周期，在此更新渐变因子的值
        if(mEffectAuto) {
            setGradientFactorValue(getGradientFactorValue() + mGradientFactorUnitValue);
            if(mGradientFactorValue > mGradientFactorMaxValue) {
                mGradientFactorValue = mGradientFactorMinValue;
            }
        }
    }

    @Override
    public void onDraw(ImgTexFrame[] frames) {
        for (int i = 0; i < mVTexInfos.size(); i++) {
            if (frames[i + 1] != null) {
                mVTexInfos.get(i).texture[0] = frames[i + 1].textureId;
            }
        }
        super.onDraw(frames);
    }

    @Override
    public void onDrawArraysAfter() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    protected void onDisconnect(boolean recursive) {
        super.onDisconnect(recursive);
        mGradientFactorValue = mGradientFactorMinValue;
    }

    @Override
    public void onRelease() {
        super.onRelease();
        for (int i = 0; i < getVSinkPinNum(); i++) {
            TextureInfo textureInfo = mVTexInfos.get(i);
            if (textureInfo.textureLoc >= 0) {
                GLES20.glDeleteTextures(1, textureInfo.texture, 0);
                textureInfo.texture[0] = ImgTexFrame.NO_TEXTURE;
            }
        }
    }

    @Override
    public void onInitialized() {
        if (!TextUtils.isEmpty(mGradientName)) {
            try {
                mGradientLoc = getUniformLocation(mGradientName);
            } catch (RuntimeException e) {

            }
        }

        for (int i = 0; i < getVSinkPinNum(); i++) {
            if (!TextUtils.isEmpty(mGradientName)) {
                try {
                    TextureInfo textureInfo = mVTexInfos.get(i);
                    textureInfo.textureLoc = getUniformLocation(textureInfo.textureName);
                } catch (RuntimeException e) {

                }
            }
        }
    }

    private class TextureInfo {
        int texture[];
        String textureName;
        int textureLoc;

        public TextureInfo(String textureName) {
            texture = new int[]{ImgTexFrame.NO_TEXTURE};
            textureLoc = -1;
            this.textureName = textureName;
        }
    }
}