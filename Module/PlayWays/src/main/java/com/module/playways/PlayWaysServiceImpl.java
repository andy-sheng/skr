package com.module.playways;

import android.app.Activity;
import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.event.GrabChangeRoomEvent;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.activity.GrabRoomActivity;
import com.module.playways.rank.prepare.model.JoinGrabRoomRspModel;
import com.module.playways.rank.room.fragment.LeaderboardFragment;
import com.module.rank.IRankingModeService;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.common.rxretrofit.ApiManager.APPLICATION_JSON;

@Route(path = RouterConstants.SERVICE_RANKINGMODE, name = "测试服务")
public class PlayWaysServiceImpl implements IRankingModeService {
    public final static String TAG = "ChannelServiceImpl";

    /**
     * 主要返回的是只在 channel 自定义类型，注意在 commonservice 中增加接口，
     * 如是一个自定义view，增加自定义view需要的接口即可
     * 如果是一个实体类，可以简单的直接移动到 commonservice 相应的包下
     */
    @Override
    public Object getData(int type, Object object) {
        return null;
    }

    @Override
    public Class getLeaderboardFragmentClass() {
        return LeaderboardFragment.class;
    }

    @Override
    public void tryGoGrabRoom(int roomID) {
        GrabRoomServerApi roomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", roomID);/Users/chengsimin/dev/livesdk/livesdk/Module/PlayWays/src/main/java/com/module/playways/PlayWaysActivity.java
        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(roomServerApi.joinGrabRoom(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    JoinGrabRoomRspModel grabCurGameStateModel = JSON.parseObject(result.getData().toString(), JoinGrabRoomRspModel.class);
                    for (Activity activity : U.getActivityUtils().getActivityList()) {
                        if (activity instanceof GrabRoomActivity) {
                            MyLog.d(TAG, " 存在一唱到底主页面了，发event刷新view");
                            EventBus.getDefault().post(new GrabChangeRoomEvent(grabCurGameStateModel));
                            return;
                        }
                    }
                    // 页面不存在 跳转 到一唱到底页面
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
                            .withSerializable("prepare_data", grabCurGameStateModel)
                            .navigation();
                } else {
                    U.getToastUtil().showShort(result.getErrmsg());
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
            }
        });
    }

    @Override
    public void init(Context context) {

    }


}
