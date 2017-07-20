package com.wali.live.watchsdk.component;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.view.BackTitleBar;
import com.live.module.common.R;
import com.wali.live.component.presenter.adapter.SingleChooser;
import com.wali.live.watchsdk.component.adapter.EnvelopeChooser;
import com.wali.live.watchsdk.component.view.EnvelopeTypeView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangli on 2017/7/18.
 *
 * @module 发红包
 */
public class SendEnvelopeFragment extends BaseFragment implements View.OnClickListener {

    private BackTitleBar mTitleBar;
    private TextView mGoldDiamondTv;
    private TextView mSilverDiamondTv;

    private final EnvelopeChooser mEnvelopeChooser = new EnvelopeChooser(new SingleChooser.IChooserListener() {
        @Override
        public void onItemSelected(View view) {
            if (!view.isSelected()) {
                view.setSelected(true);
            }
        }
    });

    protected final void $click(int id, View.OnClickListener listener) {
        $(id).setOnClickListener(listener);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.send_btn) {
            // todo 加入发红包逻辑
        } else if (i == R.id.instruction_btn) {
//            Intent intent = new Intent(getActivity(), WebViewActivity.class);
//            intent.putExtra(WebViewActivity.EXTRA_URL, AppCommonUtils.getWebViewUrl(RED_ENVELOPE_DESC));
//            startActivity(intent);
        } else if (i == R.id.recharge) {
//            Bundle bundle = new Bundle();
//            bundle.putInt(StatisticsKey.Recharge.RECHARGE_FROM, StatisticsKey.Recharge.FROM_ROOM);
//            RechargeActivity.openActivity(getActivity(), bundle);
//            StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, "redEnvelope-recharge-" + zuId + "-click", 1);
        }
    }

    @Override
    public int getRequestCode() {
        return mRequestCode;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.send_envelope_fragment, container, false);
    }

    private List<View> addEnvelopeView(ViewGroup viewGroup, EnvelopeTypeView.EnvelopeType... envelopeTypes) {
        List<View> views = new LinkedList<>();
        for (EnvelopeTypeView.EnvelopeType elem : envelopeTypes) {
            EnvelopeTypeView envelopeTypeView = new EnvelopeTypeView(viewGroup.getContext());
            envelopeTypeView.setData(elem);
            viewGroup.addView(envelopeTypeView);
            views.add(envelopeTypeView);
        }
        return views;
    }

    @Override
    protected void bindView() {
        mTitleBar = $(R.id.title_bar);
        mTitleBar.setTitle(R.string.send_redpacket);
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.color_ff5345));
        mTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ViewGroup container1 = $(R.id.envelope_container1);
        List<View> envelopeViews = addEnvelopeView(container1,
                new EnvelopeTypeView.EnvelopeType(666, R.color.color_white, R.drawable.red_item_0),
                new EnvelopeTypeView.EnvelopeType(999, R.color.color_white, R.drawable.red_item_1),
                new EnvelopeTypeView.EnvelopeType(2800, R.color.color_white, R.drawable.red_item_2));
        ViewGroup container2 = $(R.id.envelope_container2);
        envelopeViews.addAll(addEnvelopeView(container2,
                new EnvelopeTypeView.EnvelopeType(5200, R.color.color_ffda7b_90, R.drawable.red_item_3),
                new EnvelopeTypeView.EnvelopeType(6888, R.color.color_ffda7b_90, R.drawable.red_item_4),
                new EnvelopeTypeView.EnvelopeType(8888, R.color.color_ffda7b_90, R.drawable.red_item_5)));
        mEnvelopeChooser.setup(envelopeViews, envelopeViews.get(0));

        mGoldDiamondTv = $(R.id.gold_diamond_tv);
        mSilverDiamondTv = $(R.id.silver_diamond_tv);

        $click(R.id.send_btn, this);
        $click(R.id.instruction_btn, this);
        $click(R.id.recharge, this);
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    public static void openFragment(BaseActivity activity) {
        FragmentNaviUtils.addFragment(activity, R.id.main_act_container, SendEnvelopeFragment.class,
                null, true, true, true);
    }
}
