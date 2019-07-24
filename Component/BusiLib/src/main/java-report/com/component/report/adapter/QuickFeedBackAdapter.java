package com.component.report.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.common.callback.Callback;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.DiffAdapter;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.imagebrowse.ImageBrowseView;
import com.imagebrowse.big.BigImageBrowseFragment;
import com.imagebrowse.big.DefaultImageBrowserLoader;
import com.respicker.model.ImageItem;

import java.util.List;

public class QuickFeedBackAdapter extends DiffAdapter<ImageItem, RecyclerView.ViewHolder> {
    FeedBackPicManageListener mFeedBackPicManageListener;

    public QuickFeedBackAdapter(FeedBackPicManageListener feedBackPicManageListener) {
        mFeedBackPicManageListener = feedBackPicManageListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quick_feed_back_item_layout, parent, false);
        ItemHolder viewHolder = new ItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ImageItem imageItem = mDataList.get(position);
        ItemHolder reportItemHolder = (ItemHolder) holder;
        reportItemHolder.bind(imageItem, position);

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ImageItem mImageItem;
        SimpleDraweeView mPicIv;
        ImageView mDeleteIv;
        int mPosition;

        public ItemHolder(final View itemView) {
            super(itemView);
            mPicIv = (SimpleDraweeView) itemView.findViewById(R.id.pic_iv);
            mDeleteIv = (ImageView) itemView.findViewById(R.id.delete_iv);
            mPicIv.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    if (mFeedBackPicManageListener != null) {
                        if (TextUtils.isEmpty(mImageItem.getPath())) {
                            mFeedBackPicManageListener.addPic();
                        } else {
                            BigImageBrowseFragment.open(true, (FragmentActivity) itemView.getContext(), new DefaultImageBrowserLoader<ImageItem>() {
                                @Override
                                public void init() {

                                }

                                @Override
                                public void load(ImageBrowseView imageBrowseView, int position, ImageItem item) {
                                    imageBrowseView.load(item.getPath());
                                }

                                @Override
                                public int getInitCurrentItemPostion() {
                                    return mPosition;
                                }

                                @Override
                                public List<ImageItem> getInitList() {
                                    return mFeedBackPicManageListener.getImageItemList();
                                }

                                @Override
                                public void loadMore(boolean backward, int position, ImageItem data, final Callback<List<ImageItem>> callback) {

                                }

                                @Override
                                public boolean hasMore(boolean backward, int position, ImageItem data) {

                                    return false;
                                }

                                @Override
                                public boolean hasMenu() {
                                    return false;
                                }
                            });
                        }
                    }
                }
            });

            mDeleteIv.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    if (mFeedBackPicManageListener != null) {
                        mFeedBackPicManageListener.deletePic(mImageItem);
                    }
                }
            });
        }

        public void bind(ImageItem imageItem, int position) {
            mImageItem = imageItem;
            mPosition = position;
            if (TextUtils.isEmpty(imageItem.getPath())) {
                mPicIv.setImageResource(R.drawable.feed_back_add_pic);
                mDeleteIv.setVisibility(View.GONE);
            } else {
                FrescoWorker.loadImage(mPicIv,
                        ImageFactory.newPathImage(imageItem.getPath())
                                .setCornerRadius(U.getDisplayUtils().dip2px(8))
                                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                                .setFailureDrawable(U.app().getResources().getDrawable(R.drawable.load_img_error))
                                .setLoadingDrawable(U.app().getResources().getDrawable(R.drawable.loading_place_holder_img))
                                .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_320.getW()).build())
                                .setBorderColor(Color.parseColor("#3B4E79")).build());

                mDeleteIv.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface FeedBackPicManageListener {
        void addPic();

        void deletePic(ImageItem imageItem);

        List<ImageItem> getImageItemList();
    }
}
