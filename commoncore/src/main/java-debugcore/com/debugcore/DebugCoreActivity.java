package com.debugcore;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.R;
import com.common.view.titlebar.CommonTitleBar;
import com.module.RouterConstants;

@Route(path = RouterConstants.ACTIVITY_DEBUG_CORE_ACTIVITY)
public class DebugCoreActivity extends BaseActivity {

    CommonTitleBar mTitlebar;
    LinearLayout mContentContainer;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.debug_core_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) this.findViewById(R.id.titlebar);
        mTitlebar.getLeftImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mContentContainer = (LinearLayout)this.findViewById(R.id.content_container);

        addItemView(new DebugModeControlItemView(this));
    }


    void addItemView(View view){
        mContentContainer.addView(view);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
