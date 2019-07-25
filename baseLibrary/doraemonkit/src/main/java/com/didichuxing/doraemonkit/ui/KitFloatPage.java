package com.didichuxing.doraemonkit.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.didichuxing.doraemonkit.DoraemonKit;
import com.didichuxing.doraemonkit.R;
import com.didichuxing.doraemonkit.kit.Category;
import com.didichuxing.doraemonkit.ui.base.BaseFloatPage;
import com.didichuxing.doraemonkit.ui.kit.GroupKitAdapter;
import com.didichuxing.doraemonkit.ui.kit.KitItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanglikun on 2018/10/23.
 */

public class KitFloatPage extends BaseFloatPage {
    private RecyclerView mGroupKitContainer;
    private GroupKitAdapter mGroupKitAdapter;

    @Override
    protected View onCreateView(Context context, ViewGroup view) {
        return LayoutInflater.from(context).inflate(R.layout.dk_float_kit, null);
    }

    @Override
    protected void onViewCreated(View view) {
        super.onViewCreated(view);
        initView();
    }

    private void initView() {
        mGroupKitContainer = findViewById(R.id.group_kit_container);
        mGroupKitContainer.setLayoutManager(new LinearLayoutManager(getContext()));
        mGroupKitAdapter = new GroupKitAdapter(getContext());
        List<List<KitItem>> kitLists = new ArrayList<>();
        {
            List<KitItem> l = DoraemonKit.getKitItems(Category.BIZ);
            if (l != null && !l.isEmpty()) {
                kitLists.add(l);
            }
        }
        {
            List<KitItem> l = DoraemonKit.getKitItems(Category.TOOLS);
            if (l != null && !l.isEmpty()) {
                kitLists.add(l);
            }
        }
        {
            List<KitItem> l = DoraemonKit.getKitItems(Category.PERFORMANCE);
            if (l != null && !l.isEmpty()) {
                kitLists.add(l);
            }
        }
        {
            List<KitItem> l = DoraemonKit.getKitItems(Category.UI);
            if (l != null && !l.isEmpty()) {
                kitLists.add(l);
            }
        }
        {
            List<KitItem> l = DoraemonKit.getKitItems(Category.CLOSE);
            if (l != null && !l.isEmpty()) {
                kitLists.add(l);
            }
        }
        mGroupKitAdapter.setData(kitLists);
        mGroupKitContainer.setAdapter(mGroupKitAdapter);
    }

    @Override
    public boolean onBackPressed() {
        finish();
        return true;
    }

    @Override
    public void onHomeKeyPress() {
        finish();
    }

    @Override
    public void onRecentAppKeyPress() {
        finish();
    }
}
