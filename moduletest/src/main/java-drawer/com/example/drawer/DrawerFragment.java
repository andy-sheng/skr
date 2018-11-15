package com.example.drawer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.wali.live.moduletest.R;

public class DrawerFragment extends BaseFragment {


    DrawerLayout mDrawerLayout;
    TextView mDescTv;
    NavigationView mNavigationView;
    TextView mDescTv2;


    @Override
    public int initView() {
        return R.layout.test_drawer_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mDrawerLayout = (DrawerLayout) mRootView.findViewById(R.id.drawer_layout);
        mDescTv = (TextView) mRootView.findViewById(R.id.desc_tv);
        mNavigationView = (NavigationView) mRootView.findViewById(R.id.navigation_view);
        mDescTv2 = (TextView) mRootView.findViewById(R.id.desc_tv2);

        /**
         * 也可以使用DrawerListener的子类SimpleDrawerListener,
         * 或者是ActionBarDrawerToggle这个子类
         */
        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        });

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                U.getToastUtil().showToast(item.getItemId() + " " + item.getIcon() + " click");
                return false;
            }
        });

        mDescTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }


    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    protected boolean onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        return false;
    }
}
