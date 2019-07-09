package com.module.msg.custom;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;

import io.rong.imkit.R;
import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.provider.PrivateConversationProvider;

@ConversationProviderTag(
        conversationType = "private",
        portraitPosition = 1
)
public class MyPrivateConversationProvider extends PrivateConversationProvider {
    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        View result = LayoutInflater.from(context).inflate(R.layout.custom_rc_item_base_conversation, (ViewGroup) null);
        MyPrivateConversationProvider.ViewHolder holder = new MyPrivateConversationProvider.ViewHolder();
        holder.title = (TextView) result.findViewById(R.id.rc_conversation_title);
        holder.time = (TextView) result.findViewById(R.id.rc_conversation_time);
        holder.content = (TextView) result.findViewById(R.id.rc_conversation_content);
        holder.notificationBlockImage = (ImageView) result.findViewById(R.id.rc_conversation_msg_block);
        holder.readStatus = (ImageView) result.findViewById(R.id.rc_conversation_status);
        holder.officialTagTv = result.findViewById(R.id.official_tag_tv);
        result.setTag(holder);
        return result;
    }

    @Override
    public void bindView(View view, int position, UIConversation data) {
        super.bindView(view, position, data);
        MyPrivateConversationProvider.ViewHolder holder = (MyPrivateConversationProvider.ViewHolder) view.getTag();
        if (data.getConversationTargetId().equals(UserInfoModel.USER_ID_XIAOZHUSHOU + "")) {
            holder.title.setTextColor(Color.parseColor("#7088FF"));
            holder.officialTagTv.setVisibility(View.VISIBLE);
        } else {
            holder.title.setTextColor(U.getColor(R.color.rc_text_color_primary));
            holder.officialTagTv.setVisibility(View.GONE);
        }
    }

    protected class ViewHolder extends PrivateConversationProvider.ViewHolder {
        public View officialTagTv;
    }
}
