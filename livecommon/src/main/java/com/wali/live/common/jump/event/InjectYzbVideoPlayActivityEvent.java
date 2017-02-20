package com.wali.live.common.jump.event;

import com.wali.live.common.jump.JumpEndFragmentAction;
import com.wali.live.common.jump.JumpSharePanelAction;

/**
 * Created by chengsimin on 16/9/18.
 */
//  所有需要注册到 YzbVideoPlayActivity 的action放在这
public class InjectYzbVideoPlayActivityEvent {
    public JumpSharePanelAction jumpSharePageAction;
    public JumpEndFragmentAction jumpEndFragmentAction;
    public InjectYzbVideoPlayActivityEvent() {
    }
}
