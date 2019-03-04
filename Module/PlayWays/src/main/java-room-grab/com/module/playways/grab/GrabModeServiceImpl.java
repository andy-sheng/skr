package com.module.playways.grab;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.component.busilib.constans.GameModeType;
import com.module.RouterConstants;
import com.module.grab.IGrabModeGameService;
import com.module.playways.rank.prepare.model.PrepareData;

@Route(path = RouterConstants.SERVICE_GRAB_SERVICE, name = "抢唱服务")
public class GrabModeServiceImpl implements IGrabModeGameService {
    @Override
    public Object getData(int type, Object object) {
        return null;
    }

    @Override
    public void jump(int type, Object... object) {
        if (type == 0) {
            PrepareData prepareData = new PrepareData();
            prepareData.setGameType(GameModeType.GAME_MODE_GRAB);
            prepareData.setTagId((int) object[0]);
            ARouter.getInstance()
                    .build(RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
                    .withSerializable("prepare_data", prepareData)
                    .navigation();
        }
    }

    @Override
    public void init(Context context) {

    }
}
