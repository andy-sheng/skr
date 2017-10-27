package com.wali.live.watchsdk.sixin.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.wali.live.dao.SixinMessage;
import com.wali.live.watchsdk.sixin.message.SixinMessageItem;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by anping on 16-2-23.
 */
public class SixinMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_LEFT_ITEM = 0;
    private static final int TYPE_RIGHT_ITEM = 1;
    private static final int TYPE_NOTIFICATION = 3;     // 显示通知消息
    private static final int TYPE_FOOT_ITEM = 4;        // 显示footer,消息的加载更多
    private static final int TYPE_POSTER_INFO = 5;      // 系统推送的图文消息
    private static final int TYPE_TEXT_LINK = 6;        // 系统推送的文字+链接消息
    private static final int TYPE_LIST = 7;             // VIP客服问题列表

    public List<SixinMessageItem> mDataSource = new ArrayList<>();

    public void setDataSource(List<SixinMessageItem> dataSource) {
        mDataSource = new ArrayList<>(dataSource);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        SixinMessageItem messageItem = mDataSource.get(position);

        switch (messageItem.getMsgType()) {
            case SixinMessage.S_MSG_CUSTOME_TYPE_LOADING_FOOT: {
                return TYPE_FOOT_ITEM;
            }
            case SixinMessage.S_MSG_TYPE_ENTER_GROUP: {
                return TYPE_NOTIFICATION;
            }
            case SixinMessage.S_MSG_TYPE_LEAVE_GROUP: {
                return TYPE_NOTIFICATION;
            }
            case SixinMessage.S_MSG_TYPE_QUIT_GROUP: {
                return TYPE_NOTIFICATION;
            }
            case SixinMessage.S_MSG_TYPE_POSTER: {
                return TYPE_POSTER_INFO;
            }
            case SixinMessage.S_MSG_TYPE_TEXT_LINK: {
                return TYPE_TEXT_LINK;
            }
            case SixinMessage.S_MSG_TYPE_LIST: {
                return TYPE_LIST;
            }
        }

        if (isShowLeft(messageItem)) {
            return TYPE_LEFT_ITEM;
        } else {
            return TYPE_RIGHT_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        RecyclerView.ViewHolder sixInMessageCommonViewHolder = null;
        return sixInMessageCommonViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return mDataSource == null ? 0 : mDataSource.size();
    }

    private boolean isShowLeft(SixinMessageItem messageItem) {
        return messageItem.isInbound();
    }
}
