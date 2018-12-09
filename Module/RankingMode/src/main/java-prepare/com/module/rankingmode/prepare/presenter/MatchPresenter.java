package com.module.rankingmode.prepare.presenter;

import com.common.mvp.RxLifeCyclePresenter;
import com.module.rankingmode.prepare.event.MatchStatusChangeEvent;

import org.greenrobot.eventbus.EventBus;

// 处理MatchingFragment中请求相关
public class MatchPresenter extends RxLifeCyclePresenter {

    // 开始匹配
    public void startMatch(){
        // todo 短链接向服务器发送开始匹配请求
        EventBus.getDefault().post(new MatchStatusChangeEvent(MatchStatusChangeEvent.MATCH_STATUS_MATCHING));
    }

    // 取消匹配,重新回到开始匹配
    public void cancelMatch(){
        // todo 短链接向服务器发送取消匹配请求
        EventBus.getDefault().post(new MatchStatusChangeEvent(MatchStatusChangeEvent.MATCH_STATUS_START));
    }
}
