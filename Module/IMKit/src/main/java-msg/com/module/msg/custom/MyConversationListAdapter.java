package com.module.msg.custom;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.userinfo.model.VipInfo;
import com.zq.live.proto.Common.EVIPType;

import io.rong.imkit.R;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imkit.widget.ProviderContainerView;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imlib.model.UserInfo;

public class MyConversationListAdapter extends ConversationListAdapter {

    Context mContext;

    public MyConversationListAdapter(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected View newView(Context context, int position, ViewGroup group) {
        View result = LayoutInflater.from(mContext).inflate(R.layout.custom_rc_item_conversation, (ViewGroup) null);
        MyConversationListAdapter.ViewHolder holder = new MyConversationListAdapter.ViewHolder();
        holder.layout = result.findViewById(R.id.rc_item_conversation);
        holder.leftImageLayout = result.findViewById(R.id.rc_item1);
        holder.rightImageLayout = result.findViewById(R.id.rc_item2);
        holder.leftUnReadView = result.findViewById(R.id.rc_unread_view_left);
        holder.rightUnReadView = result.findViewById(R.id.rc_unread_view_right);
        holder.leftImageView = (AsyncImageView) result.findViewById(R.id.rc_left);
        holder.rightImageView = (AsyncImageView) result.findViewById(R.id.rc_right);
        holder.contentView = (ProviderContainerView) result.findViewById(R.id.rc_content);
        holder.unReadMsgCount = (TextView) result.findViewById(R.id.rc_unread_message);
        holder.unReadMsgCountRight = (TextView) result.findViewById(R.id.rc_unread_message_right);
        holder.unReadMsgCountIcon = (ImageView) result.findViewById(R.id.rc_unread_message_icon);
        holder.unReadMsgCountRightIcon = (ImageView) result.findViewById(R.id.rc_unread_message_icon_right);
        holder.vipIv = (ImageView) result.findViewById(R.id.vip_iv);
        result.setTag(holder);
        return result;
    }

    @Override
    protected void bindView(View v, int position, UIConversation data) {
        super.bindView(v, position, data);
        MyConversationListAdapter.ViewHolder holder = (MyConversationListAdapter.ViewHolder) v.getTag();
        UserInfo target = RongUserInfoManager.getInstance().getUserInfo(data.getConversationTargetId());
        if (target!=null && !TextUtils.isEmpty(target.getExtra())) {
            JSONObject jsonObject = JSON.parseObject(target.getExtra(), JSONObject.class);
            VipInfo vipInfo = jsonObject.getObject("vipInfo", VipInfo.class);
            if (vipInfo != null) {
                if (vipInfo.getVipType() == EVIPType.EVT_RED_V.getValue()) {
                    holder.vipIv.setVisibility(View.VISIBLE);
                    holder.vipIv.setImageResource(R.drawable.vip_red_icon);
                } else if (vipInfo.getVipType() == EVIPType.EVT_GOLDEN_V.getValue()) {
                    holder.vipIv.setVisibility(View.VISIBLE);
                    holder.vipIv.setImageResource(R.drawable.vip_gold_icon);
                } else {
                    holder.vipIv.setVisibility(View.GONE);
                }
            } else {
                holder.vipIv.setVisibility(View.GONE);
            }
        }else {
            holder.vipIv.setVisibility(View.GONE);
        }
    }

    protected class ViewHolder extends ConversationListAdapter.ViewHolder {
        public ImageView vipIv;
    }
}
