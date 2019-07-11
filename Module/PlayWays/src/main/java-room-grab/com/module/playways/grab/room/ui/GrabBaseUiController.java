package com.module.playways.grab.room.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.inter.IGrabRoomView;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.GameTipsManager;
import com.module.playways.grab.room.view.IRedPkgCountDownView;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;
import com.module.playways.room.prepare.model.OnlineInfoModel;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Room.EQRoundStatus;

import java.util.List;

import static android.view.View.GONE;

/**
 * 这个 GrabBaseUiController 实际上会有两个实例 video 和 audio。
 * 所以GrabBaseUiController 按理不应有成员变量的实例，比如 A a = new A(),那这个实例会存在两个。
 * 所以类似的实例都放在Fragment中统一初始化或者各自的子类中。
 */
public abstract class GrabBaseUiController {
    public final static String TAG = "GrabBaseUiController";

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
