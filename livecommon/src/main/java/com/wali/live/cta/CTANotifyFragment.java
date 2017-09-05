package com.wali.live.cta;

import android.animation.ObjectAnimator;
import android.support.annotation.IdRes;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.live.module.common.R;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.statistics.StatisticsKey;

/**
 * Created by yaojian on 16-3-26.
 */
public class CTANotifyFragment extends BaseFragment implements View.OnClickListener {

    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    private CTANotifyButtonClickListener mButtonClickListener = null;

    private CheckBox mNeverShowCb;          //只显示一次
    private TextView mCancelButton;         //取消按钮
    private TextView mConfirmButton;            //确定按钮

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.fragment_cta_notify, container, false);
        StatisticsAlmightyWorker.getsInstance().recordDelayDefault(StatisticsKey.KEY_FACTORY_FIRSTPAGE_VIEW, 1);

        return rootView;
    }

    @Override
    protected void bindView() {
        mNeverShowCb = (CheckBox) mRootView.findViewById(R.id.never_show_cb);
        mNeverShowCb.setChecked(true);

        TextView messageTv = (TextView) mRootView.findViewById(R.id.message);
        Linkify.addLinks(messageTv, Linkify.ALL);
        messageTv.setMovementMethod(LinkMovementMethod.getInstance());
        mCancelButton = (TextView) mRootView.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(this);
        mConfirmButton = (TextView) mRootView.findViewById(R.id.agree_button);
        mConfirmButton.setOnClickListener(this);
    }

    /**
     * 设置回调
     *
     * @param listener
     */
    public void setCTANotifyButtonClickListener(CTANotifyButtonClickListener listener) {
        if (listener != null) {
            mButtonClickListener = listener;
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.cancel_button) {
            if (mButtonClickListener != null) {
                mButtonClickListener.onClickCancelButton();
            }
            StatisticsAlmightyWorker.getsInstance().recordDelayDefault(StatisticsKey.KEY_FACTORY_FIRSTPAGE_CANCEL, 1);

        } else if (i == R.id.agree_button) {
            if (mButtonClickListener != null) {
                mButtonClickListener.onClickConfirmButton(mNeverShowCb.isChecked());
            }
            StatisticsAlmightyWorker.getsInstance().recordDelayDefault(StatisticsKey.KEY_FACTORY_FIRSTPAGE_AGREE, 1);

        }
    }

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    public static void openFragment(BaseSdkActivity activity, @IdRes int containerId, CTANotifyButtonClickListener listener) {
        CTANotifyFragment fragment = (CTANotifyFragment) FragmentNaviUtils.addFragment(activity, containerId, CTANotifyFragment.class, null, true, false, true);
        fragment.setCTANotifyButtonClickListener(listener);
    }

    /**
     * 按钮点击事件的回调
     */
    public interface CTANotifyButtonClickListener {
        /**
         * 点击取消按钮
         */
        void onClickCancelButton();

        /**
         * 点击确定按钮
         *
         * @param neverShow true只显示一次
         */
        void onClickConfirmButton(boolean neverShow);
    }
}
