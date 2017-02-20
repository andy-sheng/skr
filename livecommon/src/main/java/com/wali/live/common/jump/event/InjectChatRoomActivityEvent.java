package com.wali.live.common.jump.event;

import com.wali.live.common.jump.JumpFloatHomePageAction;

/**
 * Created by chengsimin on 16/9/18.
 */
//  所有需要注册到 ChatRoomActivity 的action放在这
public class InjectChatRoomActivityEvent {
    public JumpFloatHomePageAction jumpFloatHomePageAction;

    public InjectChatRoomActivityEvent(JumpFloatHomePageAction jumpAction) {
        this.jumpFloatHomePageAction = jumpAction;
    }
}
