package com.wali.live.livesdk.live.window;

/**
 * Created by yangli on 2016/12/10.
 *
 * @author YangLi
 * @mail yanglijd@gmail.com
 */
public interface IGameFloatPresenter {

    void adjustIconForInputShow(int y);

    void takeScreenshot();

    void muteMic(boolean isMute);

    void showFace(boolean isShow);

    boolean isMuteMic();

    boolean isShowFace();

    void backToApp();

    void showConfirmDialog();

    void sendBarrage(String msg, boolean isFlyBarrage);

    void onOrientation(boolean isLandscape);
}
