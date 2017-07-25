package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.log.MyLog;
import com.mi.live.data.account.channel.HostChannelManager;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.watchsdk.R;

import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * Created by wangmengjie on 17-7-24.
 *
 * @module 底部输入框
 */
public class BarrageBtnView extends FrameLayout
        implements IComponentView<BarrageBtnView.IPresenter, BarrageBtnView.IView> {
    private static final String TAG = "BarrageBtnView";

    protected IPresenter mPresenter;

    protected TextView mBarrageBtnViewTv;
    protected ImageView mBarrageBtnViewIv;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public BarrageBtnView(@NonNull Context context) {
        this(context, null);
    }

    public BarrageBtnView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarrageBtnView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.barrage_btn_view, this);

        mBarrageBtnViewTv = $(R.id.barrage_btn_view_txt);
        mBarrageBtnViewIv = $(R.id.barrage_btn_view_img);

        mBarrageBtnViewTv.setHint(getResources().getString(R.string.empty_edittext_hint));
        $click(mBarrageBtnViewTv, new OnClickListener() {
            @Override
            public void onClick(View view) {
                MyLog.w(TAG, "open inputview!");
                mPresenter.showInputView();
                String msgType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SENDMESSAGE;
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                            String.format(msgType, HostChannelManager.getInstance().getChannelId()),
                            TIMES, "1");
            }
        });
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView{

            @Override
            public <T extends View> T getRealView() {
                return (T) BarrageBtnView.this;
            }
        }
        return new ComponentView();
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
