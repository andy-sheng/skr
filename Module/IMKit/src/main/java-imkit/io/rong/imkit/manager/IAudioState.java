//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.manager;

import io.rong.imkit.manager.AudioStateMessage;

public abstract class IAudioState {
    public IAudioState() {}

    void enter() {}

    abstract void handleMessage(AudioStateMessage var1);
}
