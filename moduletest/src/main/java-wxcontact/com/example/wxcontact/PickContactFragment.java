package com.example.wxcontact;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.indexrecyclerview.IndexableLayout;
import com.indexrecyclerview.adapter.IndexableHeaderAdapter;
import com.indexrecyclerview.adapter.SimpleFooterAdapter;
import com.indexrecyclerview.adapter.SimpleHeaderAdapter;
import com.wali.live.moduletest.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PickContactFragment extends BaseFragment {

    IndexableLayout mIndexableLayout;
    private ContactAdapter mAdapter;
    MenuHeaderAdapter mMenuHeaderAdapter;

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public int initView() {
        return R.layout.test_wxcontact_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIndexableLayout = (IndexableLayout) mRootView.findViewById(R.id.indexableLayout);
        mIndexableLayout.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ContactAdapter(getContext());
        mIndexableLayout.setAdapter(mAdapter);

        /**
         * 明天跟着所有方法走一遍
         */
        mAdapter.setDatas(initDatas());

        mIndexableLayout.setOverlayStyle_MaterialDesign(Color.RED);

        // 全字母排序。  排序规则设置为：每个字母都会进行比较排序；速度较慢
        mIndexableLayout.setCompareMode(IndexableLayout.MODE_ALL_LETTERS);

        // 添加我关心的人
        mIndexableLayout.addHeaderAdapter(new SimpleHeaderAdapter<>(mAdapter, "☆", "我关心的", initFavDatas()));

        // 构造函数里3个参数,分别对应 (IndexBar的字母索引, IndexTitle, 数据源), 不想显示哪个就传null, 数据源传null时,代表add一个普通的View
        mMenuHeaderAdapter = new MenuHeaderAdapter("↑", null, initMenuDatas());
        mIndexableLayout.addHeaderAdapter(mMenuHeaderAdapter);

        // FooterView
        mIndexableLayout.addFooterAdapter(new SimpleFooterAdapter<>(mAdapter, "尾", "我是FooterView", initFavDatas()));
    }

    private List<UserEntity> initDatas() {
        List<UserEntity> list = new ArrayList<>();
        // 初始化数据
        List<String> contactStrings = Arrays.asList(getResources().getStringArray(R.array.contact_array));
        List<String> mobileStrings = Arrays.asList(getResources().getStringArray(R.array.mobile_array));
        for (int i = 0; i < contactStrings.size(); i++) {
            UserEntity contactEntity = new UserEntity(contactStrings.get(i), mobileStrings.get(i));
            list.add(contactEntity);
        }
        return list;
    }

    private List<UserEntity> initFavDatas() {
        List<UserEntity> list = new ArrayList<>();
        list.add(new UserEntity("张三", "10000"));
        list.add(new UserEntity("李四", "10001"));
        return list;
    }

    private List<MenuEntity> initMenuDatas() {
        List<MenuEntity> list = new ArrayList<>();
        list.add(new MenuEntity("新的朋友", R.mipmap.icon_1));
        list.add(new MenuEntity("群聊", R.mipmap.icon_2));
        list.add(new MenuEntity("标签", R.mipmap.icon_3));
        list.add(new MenuEntity("公众号", R.mipmap.icon_4));
        return list;
    }

    /**
     * 自定义的MenuHeader
     */
    class MenuHeaderAdapter extends IndexableHeaderAdapter<MenuEntity> {
        private static final int TYPE = 1;

        public MenuHeaderAdapter(String index, String indexTitle, List<MenuEntity> datas) {
            super(index, indexTitle, datas);
        }

        @Override
        public int getItemViewType() {
            return TYPE;
        }

        @Override
        public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent) {
            return new VH(LayoutInflater.from(getContext()).inflate(R.layout.header_contact_menu, parent, false));
        }

        @Override
        public void onBindContentViewHolder(RecyclerView.ViewHolder holder, MenuEntity entity) {
            VH vh = (VH) holder;
            vh.tv.setText(entity.getMenuTitle());
            vh.img.setImageResource(entity.getMenuIconRes());
        }

        private class VH extends RecyclerView.ViewHolder {
            private TextView tv;
            private ImageView img;

            public VH(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.tv_title);
                img = (ImageView) itemView.findViewById(R.id.img);
            }
        }
    }
}
