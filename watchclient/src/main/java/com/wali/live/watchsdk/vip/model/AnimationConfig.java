package com.wali.live.watchsdk.vip.model;

import android.util.SparseArray;

import com.wali.live.proto.LiveCommonProto;

import java.util.List;

import static com.wali.live.watchsdk.vip.view.SuperLevelUserEnterAnimControlView.VIP_ENTER_ROOM_EFFECT_ENABLE_MIN_LEVEL;

/**
 * Created by zhujianning on 18-6-30.
 */

public class AnimationConfig {
    public final static int TYPE_ANIME_ENTER_ROOM = 1;
    public final static int TYPE_ANIME_LEVEL_UPGRAGE = 2;

    public final static int EFFECT_LEVEL_1 = 1;
    public final static int EFFECT_LEVEL_2 = 2;
    public final static int EFFECT_LEVEL_3 = 3;

    public int animaType;
    /**
     * 效果等级 -> 适用等级范围[[0],[1]]
     */
    public SparseArray<int[]> levelRange = new SparseArray<>();


    public AnimationConfig(int animaType) { //默认都不显示动画
        levelRange.clear();
        this.animaType = animaType;
        switch (animaType) {
            case TYPE_ANIME_ENTER_ROOM: {
                levelRange.put(EFFECT_LEVEL_1, new int[]{VIP_ENTER_ROOM_EFFECT_ENABLE_MIN_LEVEL, 4});
                levelRange.put(EFFECT_LEVEL_2, new int[]{5, 6});
                levelRange.put(EFFECT_LEVEL_3, new int[]{7, Integer.MAX_VALUE});
            }
            break;
            case TYPE_ANIME_LEVEL_UPGRAGE: {
                levelRange.put(TYPE_ANIME_LEVEL_UPGRAGE, new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE});
            }
            break;
        }
    }

    public void updateAnimationConfig(List<LiveCommonProto.AnimationConfig> animationConfigs, int type) { //解析服务器的数据


        if (animationConfigs != null && animationConfigs.size() > 0) {
            levelRange.clear();
            animaType = type;

            for (LiveCommonProto.AnimationConfig item : animationConfigs) {
                if (item != null && item.getLevelRange() != null) {
                    levelRange.put(item.getAnimationEffect(), new int[]{item.getLevelRange().getStart(), item.getLevelRange().getEnd()});
                }
            }
        }
    }
}
