package com.wali.live.watchsdk.component.view.panel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.log.MyLog;
import com.mi.live.data.account.channel.HostChannelManager;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.statistics.StatisticsKey;

import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * Created by zhujianning on 18-8-9.
 */

public class BaseBarrageBtnView extends FrameLayout implements
        IComponentView<BaseBarrageBtnView.IPresenter, BaseBarrageBtnView.IView> {
    private static final String TAG = "BaseBarrageBtnView";

    protected TextView mBarrageBtnViewTv;
    protected ImageView mBarrageBtnViewIv;

    protected IPresenter mPresenter;

    public BaseBarrageBtnView(@NonNull Context context) {
        super(context);
    }

    public BaseBarrageBtnView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseBarrageBtnView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void optStatistics() {
        MyLog.w(TAG, "open input view!");
        String msgType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SENDMESSAGE;
        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                String.format(msgType, HostChannelManager.getInstance().getChannelId()),
                TIMES, "1");
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) BaseBarrageBtnView.this;
            }
        }
        return new ComponentView();
    }

    @Override
    public void setPresenter(IPresenter iPresenter) {

    }

    public interface IPresenter {
        /**
         * 显示输入框
         */
        void showInputView();
    }

    public interface IView extends IViewProxy {
    }
}
