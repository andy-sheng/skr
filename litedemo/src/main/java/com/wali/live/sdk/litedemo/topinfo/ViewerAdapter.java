package com.wali.live.sdk.litedemo.topinfo;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.liveassistant.avatar.AvatarUtils;
import com.mi.liveassistant.room.viewer.model.Viewer;
import com.wali.live.sdk.litedemo.R;
import com.wali.live.sdk.litedemo.fresco.FrescoWorker;
import com.wali.live.sdk.litedemo.fresco.image.ImageFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lan on 15-11-26.
 */
public class ViewerAdapter extends RecyclerView.Adapter<ViewerAdapter.ViewerHolder> {
    private static final String TAG = ViewerAdapter.class.getSimpleName();

    private List<Viewer> mViewerList = new LinkedList<>();

    public ViewerAdapter() {
    }

    public void setViewerList(List<Viewer> dataList) {
        mViewerList.clear();
        mViewerList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void addViewerList(Collection<Viewer> dataList) {
        mViewerList.addAll(dataList);
        notifyDataSetChanged();
    }

    public Viewer getViewer(int position) {
        return mViewerList.get(position);
    }


    @Override
    public int getItemCount() {
        return mViewerList == null ? 0 : mViewerList.size();
    }

    @Override
    public ViewerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_viewer_item, parent, false);
        return new ViewerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewerHolder holder, final int position) {
        Viewer viewer = mViewerList.get(position);
        holder.bindView(viewer);
    }

    public static class ViewerHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView mViewerDv;

        public ViewerHolder(View itemView) {
            super(itemView);
            mViewerDv = (SimpleDraweeView) itemView.findViewById(R.id.viewer_dv);
        }

        protected void bindView(Viewer viewer) {
            String avatarUrl = AvatarUtils.getAvatarUrlByUid(viewer.getUid(), viewer.getAvatar());
            Log.d(TAG, "updateAnchorView avatarUrl=" + avatarUrl);
            FrescoWorker.loadImage(mViewerDv, ImageFactory.newHttpImage(avatarUrl).setIsCircle(true).build());
        }
    }
}
