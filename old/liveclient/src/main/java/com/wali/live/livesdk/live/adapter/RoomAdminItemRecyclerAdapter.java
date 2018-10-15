package com.wali.live.livesdk.live.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.mi.live.data.user.User;
import com.wali.live.livesdk.R;
import com.wali.live.utils.AvatarUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuxiaoshan on 16-6-13.
 */
public class RoomAdminItemRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private List<User> dataList;

    /**
     * 数据类型为：管理员
     */
    public static final int DATA_TYPE_ADMIN = 1;
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
        if (viewType == DATA_TYPE_ADMIN) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_admin_item, parent, false);
            AdminHolder aHolder = new AdminHolder(view);
            holder = aHolder;
        } else if (viewType == DATA_TYPE_BANSPEAKER) {
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

        if (getItemViewType(position) == DATA_TYPE_FOOTER) {
            RoomAdminTipsHolder tipsHolder = (RoomAdminTipsHolder) vholder;
            tipsHolder.rootView.setVisibility(View.VISIBLE);
            switch (itemViewType) {
                case DATA_TYPE_ADMIN: {
                    tipsHolder.adminBtnBtn.setVisibility(View.VISIBLE);
                    tipsHolder.addAdminTv.setOnClickListener(this);
                    tipsHolder.tipsTextView.setText(GlobalData.app().getString(R.string.admin_manager_tips));
                    tipsHolder.tipsTextView2.setText(GlobalData.app().getString(R.string.admin_manager_tips_1));
                    tipsHolder.tipsTextView3.setText(GlobalData.app().getString(R.string.admin_manager_tips_2));
                    tipsHolder.tipsTextView4.setText(GlobalData.app().getString(R.string.admin_manager_tips_3));
                    tipsHolder.tipsTextView4.setVisibility(View.VISIBLE);
                    break;
                }
                case DATA_TYPE_BANSPEAKER: {
                    tipsHolder.adminBtnBtn.setVisibility(View.GONE);
                    tipsHolder.tipsTextView.setText(GlobalData.app().getString(R.string.admin_manager_tips));
                    tipsHolder.tipsTextView2.setText(GlobalData.app().getString(R.string.admin_banspeaker_tips_1));
                    tipsHolder.tipsTextView3.setText(GlobalData.app().getString(R.string.admin_banspeaker_tips_2));
                    tipsHolder.tipsTextView4.setVisibility(View.GONE);
                    break;
                }
            }
        } else {
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
                    String cancelManage = GlobalData.app().getResources().getString(R.string.cancel_manage);
                    String blockText = GlobalData.app().getResources().getString(R.string.block);
                    if (itemViewType != DATA_TYPE_ADMIN) {
                        cancelManage = GlobalData.app().getResources().getString(R.string.cancel_banspeaker);
                    }
                    String cancelText = GlobalData.app().getResources().getString(R.string.cancel);
                    if (!LiveRoomCharacterManager.getInstance().isManager(user.getUid())) {
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
                    } else {
                        myAlertDialog.setItems(new String[]{cancelManage, blockText, cancelText}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        if (itemViewType == DATA_TYPE_ADMIN) {
                                            if (mStatusObserver != null) {
                                                mStatusObserver.onRemoveAdmin(user.getUid());
                                            }
                                        } else {
                                            if (mStatusObserver != null) {
                                                mStatusObserver.onRemoveForbidSpeak(user.getUid());
                                            }
                                        }
                                        break;
                                    case 1:
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
                    }
                    myAlertDialog.show();
                }
            });
        }

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
     * 添加数据
     *
     * @param userList
     */
    public void addDataList(List<User> userList) {
        if (dataList == null)
            dataList = new ArrayList<>();
        dataList.addAll(userList);
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

        LinearLayout rootView;

        LinearLayout adminBtnBtn;

        TextView tipsTextView;

        TextView tipsTextView2;

        TextView tipsTextView3;

        TextView tipsTextView4;

        View addAdminTv;


        public RoomAdminTipsHolder(View itemView) {
            super(itemView);
            rootView = (LinearLayout)itemView.findViewById(R.id.admin_tips);
            adminBtnBtn = (LinearLayout)itemView.findViewById(R.id.add_admin_btn_area);
            tipsTextView = (TextView)itemView.findViewById(R.id.admin_tips_tv1);
            tipsTextView2 = (TextView)itemView.findViewById(R.id.admin_tips_tv2);
            tipsTextView3 = (TextView)itemView.findViewById(R.id.admin_tips_tv3);
            tipsTextView4 = (TextView)itemView.findViewById(R.id.admin_tips_tv4);
            addAdminTv = itemView.findViewById(R.id.add_admin_tv);

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

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.add_admin_tv){
            EventBus.getDefault().post(new RoomAdminAddAdminEvent());
        }
    }
}
