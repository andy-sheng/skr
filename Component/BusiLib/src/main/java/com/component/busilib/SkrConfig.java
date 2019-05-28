package com.component.busilib;

import com.component.busilib.constans.GameModeType;

public class SkrConfig {

    /**
     * 机器人资源让ios的传吧 ，android 好多机型性能太差了，一录音更卡了
     */

    private static class SkrConfigHolder {
        private static final SkrConfig INSTANCE = new SkrConfig();
    }

    private SkrConfig() {

    }

    public static final SkrConfig getInstance() {
        return SkrConfigHolder.INSTANCE;
    }

    public boolean isNeedUploadAudioForAI(int gameType) {
        if(gameType==GameModeType.GAME_MODE_CLASSIC_RANK){
            return true;
        }else if(gameType==GameModeType.GAME_MODE_GRAB){
            return false;
        }
        return false;
    }

    public boolean worksShareOpen(){
        return false;
    }

}
