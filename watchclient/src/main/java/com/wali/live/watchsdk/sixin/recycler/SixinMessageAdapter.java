package com.wali.live.watchsdk.sixin.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.global.GlobalData;
import com.wali.live.dao.SixinMessage;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;
import com.wali.live.watchsdk.sixin.message.SixinMessageItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 16-2-23.
 */
public class SixinMessageAdapter extends RecyclerView.Adapter<BaseHolder> {
    private static final int TYPE_LEFT_ITEM = 0;
    private static final int TYPE_RIGHT_ITEM = 1;
    private static final int TYPE_NOTIFICATION = 3;     // 显示通知消息
    private static final int TYPE_FOOT_ITEM = 4;        // 显示footer,消息的加载更多
    private static final int TYPE_POSTER_INFO = 5;      // 系统推送的图文消息
    private static final int TYPE_TEXT_LINK = 6;        // 系统推送的文字+链接消息
    private static final int TYPE_LIST = 7;             // VIP客服问题列表

    public List<SixinMessageItem> mDataList = new ArrayList<>();

    public void setDataList(List<SixinMessageItem> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        SixinMessageItem messageItem = mDataList.get(position);

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
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SixinMessageHolder holder = null;
        View view;
        switch (viewType) {
            case TYPE_LEFT_ITEM:
                view = LayoutInflater.from(GlobalData.app()).inflate(R.layout.sixin_message_left_item, parent, false);
                holder = new SixinMessageHolder(view);
                break;
            case TYPE_RIGHT_ITEM:
                view = LayoutInflater.from(GlobalData.app()).inflate(R.layout.sixin_message_right_item, parent, false);
                holder = new SixinMessageHolder(view);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        holder.bindModel(mDataList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    private boolean isShowLeft(SixinMessageItem messageItem) {
        return messageItem.isInbound();
    }
}
