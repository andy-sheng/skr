package com.mi.live.data.gift.model.giftEntity;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.FileIOUtils;
import com.wali.live.dao.Gift;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin on 16/6/18.
 *
 * @module 礼物
 */
public class BigAnimationGift extends Gift {
    public static final String TAG = "BigAnimationGift";
    String animationBackgroundName;
    String animationForegroundName;
    List<AnimationStep> animationListForPortrait = new ArrayList<>(1);

    List<AnimationStep> animationListForLandscape = new ArrayList<>(1);

    LayoutConfig backgroundLandscapeConfig;
    LayoutConfig backgroundPortraitConfig;
    LayoutConfig foregroundLandscapeConfig;
    LayoutConfig foregroundPortraitConfig;

    public LayoutConfig getForegroundPortraitConfig() {
        return foregroundPortraitConfig;
    }

    public LayoutConfig getForegroundLandscapeConfig() {
        return foregroundLandscapeConfig;
    }

    public LayoutConfig getBackgroundPortraitConfig() {
        return backgroundPortraitConfig;
    }

    public LayoutConfig getBackgroundLandscapeConfig() {
        return backgroundLandscapeConfig;
    }


    public String getAnimationBackgroundName() {
        return animationBackgroundName;
    }

    public String getAnimationForegroundName() {
        return animationForegroundName;
    }

    public List<AnimationStep> getAnimationListForPortrait() {
        return animationListForPortrait;
    }

    public List<AnimationStep> getAnimationListForLandscape() {
        return animationListForLandscape;
    }

    public void completeGiftInfo(String jsonConfigPath) {
        if (!animationListForPortrait.isEmpty()
                || !animationListForLandscape.isEmpty()
                || backgroundLandscapeConfig != null
                || backgroundPortraitConfig != null
                || foregroundLandscapeConfig != null
                || foregroundPortraitConfig != null) {
            return;
        }

        JSONObject jsonObject = null;
        try {
            MyLog.d(TAG, "jsonConfigPath:" + jsonConfigPath);
            String jsonStr = FileIOUtils.readFile(jsonConfigPath);
            MyLog.d(TAG, "jsonStr:" + jsonStr);
            jsonObject = new JSONObject(jsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jsonObject != null) {
            File resFile = new File(jsonConfigPath).getParentFile();
            {
                String name = jsonObject.optString("animationBackgroundName");
                if (!TextUtils.isEmpty(name)) {
                    animationBackgroundName = new File(resFile, name).getAbsolutePath();
                }
            }
            {
                String name = jsonObject.optString("animationForegroundName");
                if (!TextUtils.isEmpty(name)) {
                    animationForegroundName = new File(resFile, name).getAbsolutePath();
                }
            }
            {
                JSONArray jsonArray = jsonObject.optJSONArray("animationList");
                loadAnimationList(jsonArray, animationListForPortrait, resFile);
            }
            {
                JSONArray jsonArray = jsonObject.optJSONArray("animationListForLandscape");
                loadAnimationList(jsonArray, animationListForLandscape, resFile);
            }
            {
                JSONObject jsonObject1 = jsonObject.optJSONObject("backgroundLandscapeConfig");
                if (jsonObject1 != null) {
                    backgroundLandscapeConfig = new LayoutConfig();
                    load(jsonObject1, backgroundLandscapeConfig, resFile);
                }
            }
            {
                JSONObject jsonObject1 = jsonObject.optJSONObject("backgroundPortraitConfig");
                if (jsonObject1 != null) {
                    backgroundPortraitConfig = new LayoutConfig();
                    load(jsonObject1, backgroundPortraitConfig, resFile);
                }
            }
            {
                JSONObject jsonObject1 = jsonObject.optJSONObject("foregroundLandscapeConfig");
                if (jsonObject1 != null) {
                    foregroundLandscapeConfig = new LayoutConfig();
                    load(jsonObject1, foregroundLandscapeConfig, resFile);
                }
            }
            {
                JSONObject jsonObject1 = jsonObject.optJSONObject("foregroundPortraitConfig");
                if (jsonObject1 != null) {
                    foregroundPortraitConfig = new LayoutConfig();
                    load(jsonObject1, foregroundPortraitConfig, resFile);
                }
            }
        }
    }


    private void loadAnimationList(JSONArray jsonArray, List<AnimationStep> animationList, File resFile) {
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject animationStepJson = jsonArray.optJSONObject(i);
                if (animationStepJson != null) {
                    AnimationStep animationStep = new AnimationStep();
                    String name = animationStepJson.optString("animationName");
                    if (!TextUtils.isEmpty(name)) {
                        animationStep.animationName = new File(resFile, animationStepJson.optString("animationName")).getAbsolutePath();
                    }
                    JSONObject sizeObject = animationStepJson.optJSONObject("animationSize");
                    if (sizeObject != null) {
                        animationStep.width = sizeObject.optInt("width");
                        animationStep.height = sizeObject.optInt("height");
                    }
                    JSONArray animationsJsonArray = animationStepJson.optJSONArray("animations");
                    if (animationsJsonArray != null) {
                        for (int j = 0; j < animationsJsonArray.length(); j++) {
                            AnimationStep.Step step = new AnimationStep.Step();
                            JSONObject pathObj = animationsJsonArray.optJSONObject(j);
                            if (pathObj != null) {
                                JSONObject startPoint = pathObj.optJSONObject("startPoint");
                                JSONObject endPoint = pathObj.optJSONObject("endPoint");
                                step.duration = pathObj.optDouble("duration");
                                step.sx = startPoint.optDouble("x");
                                step.sy = startPoint.optDouble("y");
                                step.sscale = startPoint.optDouble("scale");
                                step.ex = endPoint.optDouble("x");
                                step.ey = endPoint.optDouble("y");
                                step.escale = endPoint.optDouble("scale");
                                animationStep.animations.add(step);
                            }
                        }
                    }
                    animationList.add(animationStep);
                }
            }
        }
    }

    private void load(JSONObject jsonObject, LayoutConfig config, File resFile) {
        {
            String name = jsonObject.optString("animationName");
            if (!TextUtils.isEmpty(name)) {
                config.animationPath = new File(resFile, name).getAbsolutePath();
            }
        }
        {
            String name = jsonObject.optString("animationNameLow");
            if (!TextUtils.isEmpty(name)) {
                config.animationLowPath = new File(resFile, name).getAbsolutePath();
            }
        }
        config.width = jsonObject.optInt("width");
        config.height = jsonObject.optInt("height");
        config.top = jsonObject.optDouble("top");
        config.left = jsonObject.optDouble("left");
    }

    public String getConfigJsonFileName() {
        // 有文件的子类覆盖这个方法
        return "android_animation.json";
    }

    public static class AnimationStep {
        String animationName;
        List<Step> animations = new ArrayList<>(0);
        int width;
        int height;

        public String getAnimationName() {
            return animationName;
        }

        public List<Step> getAnimations() {
            return animations;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public static class Step {
            public double sx;
            public double sy;
            public double sscale;
            public double ex;
            public double ey;
            public double escale;
            public double duration; //单位s

            @Override
            public String toString() {
                return "Step{" +
                        "sx=" + sx +
                        ", sy=" + sy +
                        ", sscale=" + sscale +
                        ", ex=" + ex +
                        ", ey=" + ey +
                        ", escale=" + escale +
                        ", duration=" + duration +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "AnimationStep{" +
                    "height=" + height +
                    ", width=" + width +
                    ", animations=" + animations +
                    ", animationName='" + animationName + '\'' +
                    '}';
        }
    }

    public static class LayoutConfig {
        public String animationPath;
        public String animationLowPath;
        public int width = -1;
        public int height = -1;
        public double top = 0;
        public double left = 0;
    }

    public boolean needDownResource() {
        return true;
    }

    @Override
    public String toString() {
        return "BigAnimationGift{" +
                "animationBackgroundName='" + animationBackgroundName + '\'' +
                ", animationForegroundName='" + animationForegroundName + '\'' +
                ", animationList=" + animationListForPortrait +
                '}' +
                super.toString();
    }
}
