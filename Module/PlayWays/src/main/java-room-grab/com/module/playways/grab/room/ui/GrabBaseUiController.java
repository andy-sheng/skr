package com.module.playways.grab.room.ui;

/**
 * 这个 GrabBaseUiController 实际上会有两个实例 video 和 audio。
 * 所以GrabBaseUiController 按理不应有成员变量的实例，比如 A a = new A(),那这个实例会存在两个。
 * 所以类似的实例都放在Fragment中统一初始化或者各自的子类中。
 */
public abstract class GrabBaseUiController {
    public final String TAG = "GrabBaseUiController";

    GrabRoomFragment mF;

    public GrabBaseUiController(GrabRoomFragment f) {
        mF = f;
    }

    public abstract void grabBegin();

    public abstract void singBySelf();

    public abstract void singByOthers();

    public abstract void roundOver();

    public abstract void destroy();

    public abstract void stopWork();
}
