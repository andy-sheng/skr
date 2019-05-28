//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.actions;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import java.util.List;

import io.rong.imkit.R;

public class MoreClickAdapter implements IMoreClickAdapter {
    private MoreActionLayout moreActionLayout;

    public MoreClickAdapter() {
    }

    public void bindView(ViewGroup viewGroup, Fragment fragment, List<IClickActions> actions) {
        if (this.moreActionLayout == null) {
            Context context = viewGroup.getContext();
            this.moreActionLayout = (MoreActionLayout) LayoutInflater.from(context).inflate(R.layout.rc_ext_actions_container, (ViewGroup) null);
            this.moreActionLayout.setFragment(fragment);
            this.moreActionLayout.addActions(actions);
            int height = context.getResources().getDimensionPixelOffset(R.dimen.rc_ext_more_layout_height);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, height);
            this.moreActionLayout.setLayoutParams(params);
            viewGroup.addView(this.moreActionLayout);
        }

        this.moreActionLayout.setVisibility(View.VISIBLE);
    }

    public void hideMoreActionLayout() {
        this.moreActionLayout.setVisibility(View.GONE);
    }

    public void setMoreActionEnable(boolean enable) {
        this.moreActionLayout.refreshView(enable);
    }

    public boolean isMoreActionShown() {
        return this.moreActionLayout != null && this.moreActionLayout.getVisibility() == View.VISIBLE;
    }
}
