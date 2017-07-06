package com.wali.live.watchsdk.channel.holder;

import android.view.View;

import com.wali.live.watchsdk.channel.view.FoldView;
import com.wali.live.watchsdk.channel.view.MultiLineTagLayout;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelNavigateViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaomin on 17-3-10.
 * @module 长度随内容变化的tag，有各种颜色，可以折叠
 */
public class VariableLengthTagHolder extends FixedHolder {

    FoldView mFoldView;
    MultiLineTagLayout mMultiLineTagLayout;

    public VariableLengthTagHolder(View itemView) {
        super(itemView);
        mFoldView = (FoldView) itemView;
        mMultiLineTagLayout = new MultiLineTagLayout(itemView.getContext());
        mFoldView.addContentView(mMultiLineTagLayout);
    }

    @Override
    protected void initContentView() {
    }

    @Override
    protected void bindNavigateModel(ChannelNavigateViewModel viewModel) {
        final List<ChannelNavigateViewModel.NavigateItem> itemDatas = viewModel.getItemDatas();
        if (itemDatas == null || itemDatas.isEmpty()) {
            return;
        }
        List<ChannelLiveViewModel.RichText> richTexts = new ArrayList<>();
        for (int i = 0; i < itemDatas.size(); i++) {
            ChannelNavigateViewModel.NavigateItem item = itemDatas.get(i);
            richTexts.add(new ChannelLiveViewModel.RichText(item.getText(), item.getSchemeUri(), 0));
        }
        mFoldView.bindData(richTexts);
        mMultiLineTagLayout.setItemClickListener(new MultiLineTagLayout.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                jumpItem(itemDatas.get(pos));
            }
        });
    }

    public void resetToFold() {
        mFoldView.resetToFoldState();
    }
}
