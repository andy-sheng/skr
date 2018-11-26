//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.rong.imkit.R;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.UserInfo;

public class ConversationAddMemberAdapter extends BaseAdapter<UserInfo> {
    LayoutInflater mInflater;
    Boolean isDeleteState = false;
    String mCreatorId = null;
    private io.rong.imkit.widget.adapter.ConversationAddMemberAdapter.OnDeleteIconListener mDeleteIconListener;

    public ConversationAddMemberAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.isDeleteState = false;
    }

    protected View newView(Context context, int position, ViewGroup group) {
        View result = this.mInflater.inflate(R.layout.rc_item_conversation_member, (ViewGroup) null);
        io.rong.imkit.widget.adapter.ConversationAddMemberAdapter.ViewHolder holder = new io.rong.imkit.widget.adapter.ConversationAddMemberAdapter.ViewHolder();
        holder.mMemberIcon = (AsyncImageView) this.findViewById(result, R.id.icon);
        holder.mMemberName = (TextView) this.findViewById(result, 16908308);
        holder.mDeleteIcon = (ImageView) this.findViewById(result, 16908295);
        holder.mMemberDeIcon = (ImageView) this.findViewById(result, 16908296);
        result.setTag(holder);
        return result;
    }

    protected void bindView(View v, final int position, UserInfo data) {
        io.rong.imkit.widget.adapter.ConversationAddMemberAdapter.ViewHolder holder = (io.rong.imkit.widget.adapter.ConversationAddMemberAdapter.ViewHolder) v.getTag();
        if (!data.getUserId().equals("RongAddBtn") && !data.getUserId().equals("RongDelBtn")) {
            holder.mMemberIcon.setVisibility(0);
            holder.mMemberDeIcon.setVisibility(8);
            if (data.getPortraitUri() != null) {
                holder.mMemberIcon.setResource(data.getPortraitUri().toString(), R.drawable.rc_default_portrait);
            }

            if (data.getName() != null) {
                holder.mMemberName.setText(data.getName());
            } else {
                holder.mMemberName.setText("");
            }

            if (this.isDeleteState() && !data.getUserId().equals(this.getCreatorId())) {
                holder.mDeleteIcon.setVisibility(0);
                holder.mDeleteIcon.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (io.rong.imkit.widget.adapter.ConversationAddMemberAdapter.this.mDeleteIconListener != null) {
                            io.rong.imkit.widget.adapter.ConversationAddMemberAdapter.this.mDeleteIconListener.onDeleteIconClick(v, position);
                        }

                    }
                });
            } else {
                holder.mDeleteIcon.setVisibility(4);
            }
        } else {
            holder.mMemberIcon.setVisibility(4);
            holder.mMemberDeIcon.setVisibility(0);
            if (data.getUserId().equals("RongAddBtn")) {
                holder.mMemberDeIcon.setImageResource(R.drawable.rc_ic_setting_friends_add);
            } else {
                holder.mMemberDeIcon.setImageResource(R.drawable.rc_ic_setting_friends_delete);
            }

            holder.mMemberName.setVisibility(4);
            holder.mDeleteIcon.setVisibility(8);
        }

    }

    public long getItemId(int position) {
        UserInfo info = (UserInfo) this.getItem(position);
        return info == null ? 0L : (long) info.hashCode();
    }

    public void setDeleteState(boolean state) {
        this.isDeleteState = state;
    }

    public boolean isDeleteState() {
        return this.isDeleteState;
    }

    public void setCreatorId(String id) {
        this.mCreatorId = id;
    }

    public String getCreatorId() {
        return this.mCreatorId;
    }

    public void setDeleteIconListener(io.rong.imkit.widget.adapter.ConversationAddMemberAdapter.OnDeleteIconListener listener) {
        this.mDeleteIconListener = listener;
    }

    public interface OnDeleteIconListener {
        void onDeleteIconClick(View var1, int var2);
    }

    class ViewHolder {
        AsyncImageView mMemberIcon;
        TextView mMemberName;
        ImageView mDeleteIcon;
        ImageView mMemberDeIcon;

        ViewHolder() {
        }
    }
}
