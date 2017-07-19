package com.wali.live.envelope;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.BaseActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.view.BackTitleBar;
import com.live.module.common.R;
import com.wali.live.component.presenter.adapter.SingleChooser;
import com.wali.live.envelope.adapter.EnvelopeChooser;
import com.wali.live.envelope.view.EnvelopeTypeView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangli on 2017/7/18.
 *
 * @module 发红包
 */
public class SendEnvelopeFragment extends BaseFragment {

    private BackTitleBar mTitleBar;

    private final EnvelopeChooser mEnvelopeChooser = new EnvelopeChooser(new SingleChooser.IChooserListener() {
        @Override
        public void onItemSelected(View view) {
            if (!view.isSelected()) {
                view.setSelected(true);
            }
        }
    });

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
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    public static void openFragment(BaseActivity activity) {
        FragmentNaviUtils.addFragment(activity, R.id.main_act_container, SendEnvelopeFragment.class,
                null, true, true, true);
    }
}
