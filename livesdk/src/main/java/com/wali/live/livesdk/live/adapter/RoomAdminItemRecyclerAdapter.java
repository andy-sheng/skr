package com.wali.live.livesdk.live.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.mi.live.data.user.User;
import com.wali.live.livesdk.R;
import com.wali.live.utils.AvatarUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuxiaoshan on 16-6-13.
 */
public class RoomAdminItemRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<User> dataList;
    /**
     * 数据类型为：禁言列表
     */
    public static final int DATA_TYPE_BANSPEAKER = 2;

    /**
     * Footer
     */
    static final int DATA_TYPE_FOOTER = 3;

    private Activity mActivity;

    public static final class RoomAdminAddAdminEvent {
    }

    public RoomAdminItemRecyclerAdapter() {

    }

    public RoomAdminItemRecyclerAdapter(int _dataType, Activity activity) {
        itemViewType = _dataType;
        mActivity = activity;
    }

    private OnRoomStatusObserver mStatusObserver;

    public void setOnRoomStatusObserver(OnRoomStatusObserver observer) {
        mStatusObserver = observer;
    }

    private int itemViewType;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        if (viewType == DATA_TYPE_BANSPEAKER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_admin_item, parent, false);
            AdminHolder aHolder = new AdminHolder(view);
            holder = aHolder;
        } else if (viewType == DATA_TYPE_FOOTER) {
            holder = new RoomAdminTipsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.room_admin_admin_tips,
                    parent, false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vholder, int position) {

        final User user = getDataByPosition(position);
        if (user == null)
            return;
        final AdminHolder holder = (AdminHolder) vholder;
        AvatarUtils.loadAvatarByUidTs(holder.mAvatarIv, user.getUid(), user.getAvatar(), true);
        if (TextUtils.isEmpty(user.getNickname())) {
            holder.mUsernameTv.setVisibility(View.GONE);
        } else {
            holder.mUsernameTv.setText(user.getNickname());
        }

        if (TextUtils.isEmpty(user.getSign())) {
            holder.mTipsTv.setVisibility(View.GONE);
        } else {
            holder.mTipsTv.setText(user.getSign());
        }

        holder.right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAlertDialog.Builder myAlertDialog = new MyAlertDialog.Builder(holder.itemView.getContext());
                String blockText = GlobalData.app().getResources().getString(R.string.block);
                String cancelText = GlobalData.app().getResources().getString(R.string.cancel);
                myAlertDialog.setItems(new String[]{blockText, cancelText}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                if (user == null) {
                                    return;
                                }
                                if (mStatusObserver != null) {
                                    mStatusObserver.onBlockViewer(user.getUid());
                                }
                                break;
                        }
                    }
                });

                myAlertDialog.show();
            }
        });

    }

    final int FOOTER_SIZE = 1;

    @Override
    public int getItemCount() {
        return (dataList == null ? 0 : dataList.size()) + FOOTER_SIZE;
    }

    /**
     * 添加数据
     *
     * @param list
     */
    public void addData(List<User> list) {
        if (dataList == null)
            dataList = new ArrayList<>();
        dataList.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * 添加数据
     *
     * @param user
     */
    public void addData(User user) {
        if (dataList == null)
            dataList = new ArrayList<>();
        dataList.add(user);
        notifyDataSetChanged();
    }

    /**
     * 删除指定位置的数据
     *
     * @param position
     */
    public void removeData(int position) {
        if (dataList != null) {
            dataList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clearData() {
        if (dataList != null) {
            dataList.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * 删除指定uid的数据
     *
     * @param uuid
     */
    public void removeData(long uuid) {
        if (dataList != null) {
            for (int i = 0; i < dataList.size(); i++) {
                User user = dataList.get(i);
                if (user.getUid() == uuid) {
                    dataList.remove(i);
                    notifyItemRemoved(i);
                    break;
                }
            }
        }
    }


    public void changeItemViewType(int itemType) {
        if (itemType != itemViewType && dataList != null) {
            dataList.clear();
        }
        itemViewType = itemType;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getItemCount() - FOOTER_SIZE) {
            return itemViewType;
        } else {
            return DATA_TYPE_FOOTER;

        }
    }

    /**
     * 获取指定位置的数据
     *
     * @param position
     * @return
     */
    public User getDataByPosition(int position) {
        if (dataList == null || position >= dataList.size()) {
            return null;
        } else {
            return dataList.get(position);
        }
    }

    public List<User> getDataList() {
        return dataList;
    }

    public User getDataByUserId(long uuid) {
        if (dataList != null) {
            for (int i = 0; i < dataList.size(); i++) {
                User user = dataList.get(i);
                if (user.getUid() == uuid) {
                    return user;
                }
            }
        }
        return null;
    }

    public class AdminHolder extends RecyclerView.ViewHolder {

        private BaseImageView mAvatarIv;

        private TextView mUsernameTv;

        private TextView mTipsTv;


        private ImageView right;

        public AdminHolder(View itemView) {
            super(itemView);
            mAvatarIv = (BaseImageView) itemView.findViewById(R.id.user_list_avatar);
            mUsernameTv = (TextView) itemView.findViewById(R.id.txt_username);
            mTipsTv = (TextView) itemView.findViewById(R.id.txt_tip);
            right = (ImageView) itemView.findViewById(R.id.show_dialog);
        }
    }


    public class RoomAdminTipsHolder extends RecyclerView.ViewHolder {
        View mRootView;

        public RoomAdminTipsHolder(View itemView) {
            super(itemView);
            mRootView = itemView;
        }
    }


    public interface OnRoomStatusObserver {
        void onRemoveAdmin(long adminId);

        void onRemoveForbidSpeak(long forbidId);

        void onBlockViewer(long blockId);
    }

    public interface BtnOnClickListener {
        void onClick(View v, long uuid);
    }
}
