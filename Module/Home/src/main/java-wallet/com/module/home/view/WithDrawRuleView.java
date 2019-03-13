package com.module.home.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.utils.U;
import com.module.home.R;

import java.util.List;

public class WithDrawRuleView extends FrameLayout {
    LinearLayout mLlRuleContainer;

    public WithDrawRuleView(Context context) {
        this(context, null);
    }

    public WithDrawRuleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WithDrawRuleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.withdraw_view_layout, this);
        mLlRuleContainer = (LinearLayout) findViewById(R.id.ll_rule_container);
    }

    public void bindData(List<String> ruleList) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        int i = 0;
        for(String rule : ruleList){
            View view = layoutInflater.inflate(R.layout.withdraw_rule_item, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = U.getDisplayUtils().dip2px(8);
            view.setLayoutParams(layoutParams);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView tvContent = (TextView) view.findViewById(R.id.tv_content);
            title.setText(++i + ".");
            tvContent.setText(rule);
            mLlRuleContainer.addView(view);
        }
    }
}
